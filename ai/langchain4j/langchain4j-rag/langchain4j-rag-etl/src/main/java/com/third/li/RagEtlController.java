package com.third.li;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzh.BgeSmallZhEmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ETL 管道：Document（Parser 解析）→ Splitter 分块 → EmbeddingModel 嵌入 → Store 入库。
 * 与 SAA 的 ETL 三件套、Mastra 的 MDocument.chunk 同构。
 */
@RestController
public class RagEtlController {

    private final EmbeddingModel embeddingModel = new BgeSmallZhEmbeddingModel();
    private final InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

    private final String rawDocument = """
            LangChain4j 是一个 Java 生态的大模型应用框架。它提供了 ChatModel、AiServices、
            Tools、RAG、Memory 等核心抽象。其中 AiServices 用声明式接口把 AI 调用藏到
            普通方法后面；RAG 部分则把文档解析、分块、嵌入、检索组合成可插拔的管道。
            本模块演示 ETL 管道：解析原始文档，按大小与重叠递归分块，本地嵌入后入库。
            """ ;

    /** ETL：解析 → 分块 → 嵌入 → 入库，返回分块统计。 */
    @GetMapping("/ai/rag/etl")
    public String etl() {
        Document document = Document.document(rawDocument);
        List<TextSegment> segments = DocumentSplitters.recursive(120, 20).split(document);
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        store.addAll(
                segments.stream().map(s -> "etl-" + System.nanoTime()).toList(),
                embeddings,
                segments);
        return "ETL 完成：原文 " + rawDocument.length() + " 字符 → " + segments.size() + " 块 → "
                + embeddings.size() + " 向量入库";
    }

    /** 分块预览（不入库）。 */
    @GetMapping("/ai/rag/etl/preview")
    public List<String> preview() {
        Document document = Document.document(rawDocument);
        return DocumentSplitters.recursive(120, 20).split(document).stream()
                .map(TextSegment::text)
                .toList();
    }
}