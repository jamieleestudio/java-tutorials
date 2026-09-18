package com.third.li;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量存储（SimpleVectorStore + 增删查）。
 *
 * <p>{@link VectorStore} 接口统一向量库操作，{@link SimpleVectorStore} 是
 * 内存实现（无需外部数据库，适合教学）。
 *
 * <p>操作：
 * <ul>
 *   <li>{@code add(docs)} — 写入文档（自动嵌入）</li>
 *   <li>{@code similaritySearch(query)} — 相似度检索</li>
 *   <li>{@code delete(ids)} — 删除</li>
 * </ul>
 *
 * <p>与 AgentScope 的 Knowledge / Embabel 的 RAG 对照。
 */
@RestController
public class VectorStoreController {

    private final VectorStore vectorStore;

    public VectorStoreController(EmbeddingModel embeddingModel) {
        // 用 SimpleVectorStore 演示（内存版，重启丢失）
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        // 预置一些文档
        vectorStore.add(List.of(
                new Document("Spring AI 是 Spring 官方推出的 AI 应用开发框架。",
                        Map.of("topic", "spring-ai")),
                new Document("AgentScope 是阿里巴巴开源的智能体框架。",
                        Map.of("topic", "agentscope")),
                new Document("Embabel 是一个类型化建模与规划框架。",
                        Map.of("topic", "embabel")),
                new Document("RAG 通过检索增强让模型回答更准确。",
                        Map.of("topic", "rag"))));
    }

    /** 相似度检索。 */
    @GetMapping("/ai/vector/search")
    public String search(
            @RequestParam(value = "query", defaultValue = "哪个框架是阿里开源的") String query) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(2).build());
        if (docs.isEmpty()) {
            return "未检索到结果";
        }
        return docs.stream()
                .map(d -> "- [" + d.getMetadata().getOrDefault("topic", "?") + "] " + d.getText()
                        + " (score=" + String.format("%.4f", d.getScore()) + ")")
                .collect(Collectors.joining("\n"));
    }

    /** 新增文档。 */
    @GetMapping("/ai/vector/add")
    public String add(
            @RequestParam(value = "text", defaultValue = "向量数据库让语义检索成为可能") String text) {
        vectorStore.add(List.of(new Document(text, Map.of("topic", "custom"))));
        return "已添加文档：" + text;
    }
}
