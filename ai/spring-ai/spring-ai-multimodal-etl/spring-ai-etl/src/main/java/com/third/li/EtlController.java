package com.third.li;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ETL 管道（DocumentReader + TextSplitter + VectorStore）。
 *
 * <p>RAG 的"入库"环节，把原始文档加工成可检索的向量：
 * <ol>
 *   <li><b>Extract</b>：{@code MarkdownDocumentReader} 读取 Markdown/文本 → List&lt;Document&gt;</li>
 *   <li><b>Transform</b>：{@link TokenTextSplitter} 按 token 切分成小块</li>
 *   <li><b>Load</b>：{@link VectorStore#add} 嵌入并写入向量库</li>
 * </ol>
 */
@RestController
public class EtlController {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    public EtlController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    }

    /** 执行完整 ETL：读取示例 Markdown → 切分 → 入库。 */
    @GetMapping("/ai/etl")
    public String etl() {
        // 1. Extract：读取 Markdown
        var reader = new org.springframework.ai.reader.markdown.MarkdownDocumentReader(
                "classpath:/docs/rag-guide.md");
        List<Document> docs = reader.get();

        // 2. Transform：token 切分
        List<Document> chunks = new TokenTextSplitter().apply(docs);

        // 3. Load：嵌入 + 入库
        vectorStore.add(chunks);

        return "ETL 完成：原始 " + docs.size() + " 篇 → 切分 " + chunks.size() + " 块 → 已入库";
    }

    /** 检索入库后的内容。 */
    @GetMapping("/ai/etl/search")
    public String search(
            @RequestParam(value = "query", defaultValue = "RAG 是什么") String query) {
        List<Document> hits = vectorStore.similaritySearch(query);
        if (hits.isEmpty()) {
            return "未检索到结果（请先调用 /ai/etl 入库）";
        }
        return hits.stream()
                .map(d -> "- " + d.getText())
                .reduce("", (a, b) -> a + "\n" + b);
    }
}
