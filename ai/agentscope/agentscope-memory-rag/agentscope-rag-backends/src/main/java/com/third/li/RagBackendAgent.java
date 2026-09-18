package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.KnowledgeRetrievalTools;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.model.DocumentMetadata;
import io.agentscope.core.rag.model.RetrieveConfig;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 后端 Agent：演示 Agentic RAG 模式——把知识检索作为**工具**暴露给模型，
 * 模型自主决定何时检索、检索什么。
 *
 * <p>与 {@code agentscope-rag} 的 Generic RAG 区别：
 * <ul>
 *   <li>Generic RAG：每次对话前自动检索，用 {@code GenericRAGHook} 注入上下文</li>
 *   <li>Agentic RAG：模型把 {@code retrieveKnowledge} 当工具调用，
 *       只在需要时检索，更灵活、更省 token</li>
 * </ul>
 *
 * <p>用 {@link KnowledgeRetrievalTools} 把 {@link Knowledge} 包装成 {@code @Tool} 工具，
 * 注册到 {@link Toolkit}。本例同样用内存版 {@link Knowledge} 演示后端接入。
 */
@Component
public class RagBackendAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-rag-backends";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public RagBackendAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    InMemoryKnowledge knowledge = new InMemoryKnowledge();
                    knowledge.addDocuments(List.of(
                            doc("backend-1", "rag-simple 是 AgentScope 内置的简易 RAG 后端，无需向量库。"),
                            doc("backend-2", "向量后端可选 Milvus / Qdrant / Pinecone，支持语义检索。"),
                            doc("backend-3", "Hybrid 后端结合关键词与向量检索，兼顾精确与语义匹配。")
                    )).block();

                    KnowledgeRetrievalTools ragTools = new KnowledgeRetrievalTools(knowledge,
                            RetrieveConfig.builder().limit(3).scoreThreshold(0.0).build());
                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(ragTools);

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("rag-backends")
                            .sysPrompt("你是一个知识库助手，可调用 retrieveKnowledge 工具检索后端知识。")
                            .model(model)
                            .toolkit(toolkit)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("rag-backend-demo").userId("alice").build();
    }

    private static Document doc(String id, String text) {
        return new Document(new DocumentMetadata(io.agentscope.core.message.TextBlock.builder().text(text).build(), id, "chunk-0"));
    }

    static class InMemoryKnowledge implements Knowledge {
        private final List<Document> docs = new ArrayList<>();

        @Override
        public reactor.core.publisher.Mono<Void> addDocuments(List<Document> documents) {
            return reactor.core.publisher.Mono.fromRunnable(() -> docs.addAll(documents));
        }

        @Override
        public reactor.core.publisher.Mono<List<Document>> retrieve(String query, RetrieveConfig config) {
            return reactor.core.publisher.Mono.fromSupplier(() -> docs.stream()
                    .filter(d -> d.getMetadata().getContentText().contains(query)
                            || query.contains(d.getMetadata().getDocId()))
                    .limit(config.getLimit())
                    .collect(Collectors.toList()));
        }
    }
}