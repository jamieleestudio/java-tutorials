package com.third.li;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzh.BgeSmallZhEmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** RAG 问答配置：知识库入库 + EmbeddingStoreContentRetriever + AiServices。 */
@Configuration
public class RagBasicsConfig {

    private final InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
    private final EmbeddingModel embeddingModel = new BgeSmallZhEmbeddingModel();

    public interface Tutor {

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
    public Tutor tutor(ChatModel chatModel) {
        // 1. 知识库入库
        List<TextSegment> knowledge = List.of(
                TextSegment.from("LangChain4j 的 AiServices 用声明式接口封装 AI 调用。"),
                TextSegment.from("LangChain4j 的 RAG 通过 ContentRetriever 与 RetrievalAugmentor 组合实现。"),
                TextSegment.from("LangChain4j 的 AgenticScope 是多 Agent 共享的状态容器。"));
        List<Embedding> embeddings = embeddingModel.embedAll(knowledge).content();
        for (int i = 0; i < knowledge.size(); i++) {
            store.addAll(List.of("k-" + i), List.of(embeddings.get(i)), List.of(knowledge.get(i)));
        }

        // 2. 检索器：把用户问题嵌入后在知识库里找相关片段
        EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .build();

        // 3. AiServices 自动完成"检索 → 注入上下文 → 生成"
        return AiServices.builder(Tutor.class)
                .chatModel(chatModel)
                .contentRetriever(retriever)
                .build();
    }
}
