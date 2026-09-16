package com.third.li;

import com.embabel.agent.api.common.AiBuilder;
import com.embabel.common.ai.model.EmbeddingService;
import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * 摄入流水线：**扫描 → 变更检测 → 分块 → 嵌入 → 入库**。
 *
 * <p>两个工程要点：
 * <ul>
 *   <li><b>增量</b>：用内容 SHA-256 与 {@code doc_source.content_hash} 比对，
 *       没变就跳过——避免每次全量重新嵌入（这是最贵的步骤）。</li>
 *   <li><b>可重跑</b>：摄入某文档时先按 {@code doc_id} 删掉它的旧块再写新块，
 *       所以改了分块参数后重新摄入不会残留旧块。</li>
 * </ul>
 */
@Service
public class IngestService {

    private static final Logger log = LoggerFactory.getLogger(IngestService.class);

    private final Path dir;
    private final DocumentLoader loader;
    private final TextChunker chunker;
    private final ChunkStore store;
    private final AiBuilder aiBuilder;

    public IngestService(
            @Value("${app.ingest.dir}") String dir,
            DocumentLoader loader,
            TextChunker chunker,
            ChunkStore store,
            AiBuilder aiBuilder) {
        this.dir = Path.of(dir);
        this.loader = loader;
        this.chunker = chunker;
        this.store = store;
        this.aiBuilder = aiBuilder;
    }

    /** 首次运行写入示例文档，保证"拉下来就能跑"。 */
    @PostConstruct
    void seedSampleDocuments() {
        try {
            Files.createDirectories(dir);
            if (countFiles() > 0) {
                log.info("摄入目录已有文件，跳过示例写入：{}", dir.toAbsolutePath());
                return;
            }
            write(dir.resolve("embabel-overview.md"), """
                    # Embabel 概览

                    Embabel 是 Spring 创始人 Rod Johnson 发起的 JVM Agent 框架。它用强类型领域模型描述
                    输入与输出，用可复用的 Action 描述能力，再由 GOAP 规划器围绕目标动态推导执行步骤。

                    与写死工作流相比，Agent 的下一步由模型根据当前状态决定，因此能处理未预见的路径；
                    代价是不确定性变高，需要预算、护栏与可观测性来约束。

                    ## 类型化建模

                    类型既是契约也是规划依据。框架通过动作的输入类型与输出类型推导依赖关系，
                    所以把领域概念建模清楚，规划质量会直接变好。

                    类型化建模还带来可观测性：每一步的输入输出都是具名对象，调试与回放都更容易。
                    """);
            write(dir.resolve("rag-notes.md"), """
                    # RAG 实践笔记

                    ## 分块策略

                    分块是 RAG 里最容易被低估的一步。按固定字符长度硬切会把句子、表格、代码切断，
                    导致检索命中的块缺少完整语义，模型自然答不对。

                    更稳的做法是按段落聚合到目标大小，并让相邻块之间保留一段重叠，
                    这样答案即使落在边界上也能被完整召回。

                    ## 召回与重排

                    只用向量做召回时，top-1 常常不是最相关的：向量相似不等于任务相关。
                    常见做法是两阶段检索——先用向量粗召回（快、覆盖广），再用交叉编码器或 LLM 精排（准、慢）。

                    ## 增量摄入

                    重新嵌入整份语料既慢又贵。用内容哈希判断文档是否变化，只对变化的文档重新分块与嵌入，
                    可以把日常增量成本降到接近零。
                    """);
            write(dir.resolve("pgvector-notes.md"), """
                    # pgvector 使用要点

                    pgvector 用 vector(n) 存储向量，距离算子有三个：<=> 是余弦距离，<-> 是 L2 距离，
                    <#> 是负内积。注意 <=> 返回的是距离而不是相似度，相似度要用 1 - 距离 换算。

                    索引方面，HNSW 建得快、查询快，适合大多数场景；IVFFlat 建索引更快但需要先有数据。
                    数据量很小的时候索引收益不明显，但它决定了能否平滑扩到百万级。

                    过滤条件可以直接写在 WHERE 里，和向量排序组合在同一条 SQL 中执行，
                    这是内存里做余弦相似度很难做到的。
                    """);
            log.info("已写入示例文档到 {}（{} 个文件）", dir.toAbsolutePath(), countFiles());
        } catch (IOException e) {
            log.warn("写入示例文档失败：{}", e.getMessage());
        }
    }

    /** 执行一次摄入。 */
    public IngestReport run() {
        long start = System.nanoTime();
        store.ensureSchema();

        List<Path> files = listFiles();
        List<IngestReport.DocDetail> details = new ArrayList<>();
        int ingested = 0;
        int skipped = 0;

        for (Path file : files) {
            var loaded = loader.load(file, dir);
            if (loaded.isEmpty()) {
                details.add(new IngestReport.DocDetail(
                        file.getFileName().toString(), dir.relativize(file).toString(), 0, "FAILED"));
                continue;
            }
            RawDocument document = loaded.get();
            String existing = store.findContentHash(document.docId());
            if (document.contentHash().equals(existing)) {
                skipped++;
                details.add(new IngestReport.DocDetail(
                        document.docId(), document.source(), 0, "SKIPPED"));
                continue;
            }

            List<Chunk> chunks = chunker.chunk(document);
            EmbeddingService embeddings = aiBuilder.ai().withDefaultEmbeddingService();
            List<float[]> vectors = new ArrayList<>(chunks.size());
            for (Chunk chunk : chunks) {
                vectors.add(embeddings.embed(chunk.text()));
            }
            store.replaceDocument(document, chunks, vectors);
            ingested++;
            details.add(new IngestReport.DocDetail(
                    document.docId(), document.source(), chunks.size(), "INGESTED"));
        }

        return new IngestReport(
                files.size(),
                ingested,
                skipped,
                store.countChunks(),
                "chunk-size=%d, overlap=%d, 段落感知".formatted(chunker.targetSize(), chunker.overlap()),
                details,
                (System.nanoTime() - start) / 1_000_000);
    }

    /** 生成一个 PDF 放进摄入目录，用来演示 PDF 路径。 */
    public Path writeDemoPdf() {
        Path file = dir.resolve("embabel-pdf-demo.pdf");
        // 注意：Standard14 内置字体不支持中文，所以演示 PDF 用英文。
        // 要写中文 PDF 需嵌入 TTF 字体（PDType0Font.load）。
        String[] lines = {
                "Embabel Agent Framework - PDF ingestion demo",
                "",
                "This PDF was generated by PDFBox at runtime, then ingested by the pipeline.",
                "",
                "Chunking note: paragraphs are aggregated up to the target chunk size,",
                "and neighbouring chunks keep an overlap so that an answer spanning a",
                "chunk boundary can still be retrieved completely.",
                "",
                "Vector note: pgvector stores the embedding and supports HNSW indexing.",
                "Cosine distance is written as the <=> operator, and similarity is 1 - distance.",
                "",
                "Incremental note: the pipeline hashes the extracted text, so re-running",
                "ingest without changing this file will skip it entirely.",
        };
        try {
            Files.createDirectories(dir);
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.beginText();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    stream.setLeading(16f);
                    stream.newLineAtOffset(50, 750);
                    for (String line : lines) {
                        stream.showText(line);
                        stream.newLine();
                    }
                    stream.endText();
                }
                document.save(file.toFile());
            }
            log.info("已生成演示 PDF：{}", file.toAbsolutePath());
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("生成演示 PDF 失败", e);
        }
    }

    public String dirPath() {
        return dir.toAbsolutePath().toString();
    }

    private List<Path> listFiles() {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.endsWith(".md") || name.endsWith(".txt")
                                || name.endsWith(".markdown") || name.endsWith(".pdf");
                    })
                    .sorted()
                    .toList();
        } catch (IOException e) {
            log.warn("扫描目录失败：{}", e.getMessage());
            return List.of();
        }
    }

    private long countFiles() {
        return listFiles().size();
    }

    private void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }
}
