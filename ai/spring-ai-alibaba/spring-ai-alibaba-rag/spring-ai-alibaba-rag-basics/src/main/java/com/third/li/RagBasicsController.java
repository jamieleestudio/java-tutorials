package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * RAG 检索增强（三段式：Retriever + Augmenter + Generation）。
 *
 * <p>与 {@code ai/spring-ai/spring-ai-rag/spring-ai-rag-basics} 同一管道：
 * 向量检索 → 上下文增强 → 生成。差别在于知识内容 —— 这里用 SAA 相关知识，
 * 并在 Graph 里可以把 Retrieval / Generation 拆成独立节点
 * （见 {@code spring-ai-alibaba-capstone-app} 的知识库节点）。
 */
@RestController
public class RagBasicsController {

    private final ChatClient chatClient;
    private final VectorStoreDocumentRetriever retriever;
    private final VectorStore vectorStore;

    public RagBasicsController(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
        this.chatClient = chatClientBuilder.build();

        // 内存向量库 + 预置 SAA 知识
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(List.of(
                new Document("Spring AI Alibaba 的 Graph 框架源自 LangGraph 思想，用 StateGraph 声明节点与边。",
                        Map.of("topic", "graph")),
                new Document("Spring AI Alibaba 的 CompiledGraph 支持 interruptBefore 挂起和 resume 恢复。",
                        Map.of("topic", "graph")),
                new Document("Spring AI Alibaba 的 Agent Framework 提供 ReactAgent、Hook、Interceptor 体系。",
                        Map.of("topic", "agent")),
                new Document("Spring AI Alibaba 支持通过 Nacos 管理动态提示词与 MCP 注册。",
                        Map.of("topic", "nacos"))));

        this.retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(2)
                .similarityThreshold(0.0)
                .build();
    }

    /** RAG 问答：检索相关文档后交给模型生成。 */
    @GetMapping("/ai/rag")
    public String rag(
            @RequestParam(value = "message", defaultValue = "Graph 如何实现人工挂起和恢复？") String message) {
        // 1. Retrieval
        List<Document> docs = retriever.retrieve(new Query(message));
        // 2. Augmentation：把文档拼进系统上下文
        String context = docs.stream()
                .map(Document::getText)
                .reduce("", (a, b) -> a + "\n- " + b);
        // 3. Generation
        return chatClient.prompt()
                .system("你是 Spring AI Alibaba 知识助手。基于提供的上下文回答，"
                        + "上下文没有的信息要诚实说明。\n\n相关上下文：\n" + context)
                .user(message)
                .call()
                .content()
                + "\n\n[检索到的文档数]: " + docs.size();
    }
}
