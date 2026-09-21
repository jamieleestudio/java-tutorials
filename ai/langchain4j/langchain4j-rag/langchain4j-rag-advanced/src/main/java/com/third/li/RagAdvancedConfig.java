package com.third.li;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzh.BgeSmallZhEmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

/**
 * RAG 进阶：{@code DefaultRetrievalAugmentor} + 自定义 {@code QueryRouter}，
 * 按查询关键词把问题路由到不同检索源（产品知识库 / 政策知识库）——
 * 对照 SAA 的 Graph 条件边、Mastra 的 branch，检索层路由是 RAG 版的分支模式。
 */
@Configuration
public class RagAdvancedConfig {

    private final EmbeddingModel embeddingModel = new BgeSmallZhEmbeddingModel();

    public interface KnowledgeAssistant {

        String answer(String question);
    }

    @Bean
    public dev.langchain4j.model.chat.ChatModel chatModel() {
        return dev.langchain4j.model.openai.OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-chat")
                .build();
    }

    @Bean
    public KnowledgeAssistant knowledgeAssistant(ChatModel chatModel) {
        // 检索源 A：产品知识
        InMemoryEmbeddingStore<TextSegment> productStore = new InMemoryEmbeddingStore<>();
        // 检索源 B：政策知识
        InMemoryEmbeddingStore<TextSegment> policyStore = new InMemoryEmbeddingStore<>();

        ingest(productStore, List.of(
                "Spring AI Alibaba 提供本地 BGE 中文嵌入模型，无需 API key 即可向量化。",
                "LangChain4j 的 AiServices 支持把任意接口变成 AI 服务。"));
        ingest(policyStore, List.of(
                "教程仓库的所有示例仅供学习，禁止用于生产环境。",
                "仓库内容基于 Apache 2.0 协议开源。"));

        // 路由器：按关键词决定查哪个库（生产可换成 LLM 路由或元数据路由）
        QueryRouter router = query -> {
            String text = query.text().toLowerCase();
            if (text.contains("协议") || text.contains("开源") || text.contains("政策")) {
                return List.of(EmbeddingStoreContentRetriever.builder()
                        .embeddingStore(policyStore)
                        .embeddingModel(embeddingModel)
                        .build());
            }
            return List.of(EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(productStore)
                    .embeddingModel(embeddingModel)
                    .build());
        };

        // 组合为 RetrievalAugmentor（默认：查询变换 → 路由 → 检索 → 聚合 → 注入）
        DefaultRetrievalAugmentor augmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(router)
                .build();

        return AiServices.builder(KnowledgeAssistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(augmentor)
                .build();
    }

    private void ingest(InMemoryEmbeddingStore<TextSegment> store, List<String> texts) {
        List<TextSegment> segments = texts.stream().map(TextSegment::from).toList();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        for (int i = 0; i < segments.size(); i++) {
            store.addAll(List.of("seg-" + System.nanoTime() + "-" + i),
                    List.of(embeddings.get(i)), List.of(segments.get(i)));
        }
    }
}
