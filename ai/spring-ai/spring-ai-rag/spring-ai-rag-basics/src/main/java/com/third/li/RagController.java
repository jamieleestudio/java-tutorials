package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * RAG 检索增强生成（2.0 三段式管道）。
 *
 * <p>Spring AI 2.0 的 RAG 用三段式管道（替代 1.0 的 QuestionAnswerAdvisor）：
 * <ol>
 *   <li><b>Retrieval</b>：{@link VectorStoreDocumentRetriever} 向量检索相关文档</li>
 *   <li><b>Augmentation</b>：{@link ContextualQueryAugmenter} 把文档拼进 prompt</li>
 *   <li><b>Generation</b>：ChatClient 基于增强后的上下文生成回答</li>
 * </ol>
 *
 * <p>本模块演示手写三段式 RAG（更透明，方便教学对比）。
 */
@RestController
public class RagController {

    private final ChatClient chatClient;
    private final VectorStoreDocumentRetriever retriever;
    private final ContextualQueryAugmenter augmenter;

    public RagController(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
        this.chatClient = chatClientBuilder.build();

        // 内存向量库 + 预置知识
        var vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(List.of(
                new Document("Spring AI 2.0 把工具调用下沉到 ToolCallingAdvisor。", Map.of("k", "spring-ai")),
                new Document("AgentScope Java 用 MiddlewareBase 实现洋葱模型中间件链。", Map.of("k", "agentscope")),
                new Document("Embabel 用 GOAP 规划器推导动作序列。", Map.of("k", "embabel"))));

        this.retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(2)
                .similarityThreshold(0.0)
                .build();

        this.augmenter = ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build();
    }

    /** RAG：检索 + 增强 + 生成。 */
    @GetMapping("/ai/rag")
    public String rag(
            @RequestParam(value = "message", defaultValue = "AgentScope 的中间件是怎么设计的") String message) {
        // 1. Retrieval：检索相关文档
        List<Document> docs = retriever.retrieve(new Query(message));

        // 2. Augmentation：把文档拼进增强后的 Query
        Query augmented = augmenter.augment(new Query(message), docs);

        // 3. Generation：基于增强上下文生成
        String context = docs.stream()
                .map(Document::getText)
                .reduce("", (a, b) -> a + "\n- " + b);

        return chatClient.prompt()
                .system("你是一个知识问答助手。基于提供的上下文回答用户问题，"
                        + "如果上下文里没有相关信息，就诚实说明。\n\n相关上下文：\n" + context)
                .user(message)
                .call()
                .content()
                + "\n\n[增强后的查询]: " + augmented.text();
    }
}
