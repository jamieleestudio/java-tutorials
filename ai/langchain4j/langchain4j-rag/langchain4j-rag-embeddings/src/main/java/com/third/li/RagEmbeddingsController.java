package com.third.li;



import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzh.BgeSmallZhEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BGE-zh 本地嵌入 + InMemoryEmbeddingStore：embed / search 全流程，无 API key。
 */
@RestController
public class RagEmbeddingsController {

    private final EmbeddingModel embeddingModel = new BgeSmallZhEmbeddingModel();
    private final InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

    private final List<String> docs = List.of(
            "Spring AI Alibaba 的 Graph 框架用 StateGraph 声明节点与边。",
            "Mastra 的 workflow 用 createWorkflow 和 createStep 声明式编排。",
            "LangChain4j 的 AiServices 把普通接口变成 AI 服务。",
            "AgentScope 是阿里巴巴开源的 ReAct 循环 Agent 框架。");

    public RagEmbeddingsController() {
        // 启动时预置向量
        List<TextSegment> segments = docs.stream().map(TextSegment::from).toList();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        for (int i = 0; i < segments.size(); i++) {
            store.addAll(List.of("doc-" + i), List.of(embeddings.get(i)), List.of(segments.get(i)));
        }
    }

    /** 嵌入单条文本，返回向量维度。 */
    @PostMapping("/ai/rag/embed")
    public int embed(@RequestParam(defaultValue = "测试文本") String text) {
        Embedding embedding = embeddingModel.embed(text).content();
        return embedding.dimension();
    }

    /** 相似度检索。 */
    @GetMapping("/ai/rag/search")
    public List<String> search(@RequestParam(defaultValue = "工作流怎么声明？") String query) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        var results = store.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(2)
                .build());
        return results.matches().stream()
                .map(m -> "[score=" + m.score() + "] " + m.embedded().text())
                .toList();
    }

    /** 查看预置文档。 */
    @GetMapping("/ai/rag/docs")
    public List<String> docs() {
        return docs;
    }
}
