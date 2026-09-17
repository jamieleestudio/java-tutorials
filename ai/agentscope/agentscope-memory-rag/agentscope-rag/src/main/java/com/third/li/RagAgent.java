package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.rag.GenericRAGHook;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.model.DocumentMetadata;
import io.agentscope.core.rag.model.RetrieveConfig;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG Agent：用 {@link Knowledge} + {@link GenericRAGHook} 实现检索增强生成。
 *
 * <p>RAG 流程：
 * <ol>
 *   <li>对话前（{@code PreCall}），{@link GenericRAGHook} 用用户问题检索 {@link Knowledge}</li>
 *   <li>把检索到的文档拼成上下文，注入到系统提示</li>
 *   <li>模型基于"问题 + 检索上下文"生成回答</li>
 * </ol>
 *
 * <p>{@link Knowledge} 是接口（{@code addDocuments} / {@code retrieve}），需要提供具体实现——
 * 生产环境通常接向量库（Milvus/Qdrant/Pinecone）。本例用内存关键词匹配实现演示 API。
 *
 * <p>预先灌入几条"知识库文档"，Agent 回答时会自动检索相关内容。
 */
@Component
public class RagAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-rag";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public RagAgent(
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
                            doc("kb-1", "AgentScope 是阿里巴巴开源的 Agent 框架，支持 ReAct 循环和 Middleware 链。"),
                            doc("kb-2", "AgentScope 的权限引擎支持 bypass/confirm/strict 三种模式。"),
                            doc("kb-3", "AgentScope 的技能系统由 SkillBox 和 SkillRegistry 组成。")
                    )).block();

                    GenericRAGHook ragHook = new GenericRAGHook(knowledge,
                            RetrieveConfig.builder().limit(2).scoreThreshold(0.0).build());

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("rag-agent")
                            .sysPrompt("你是一个知识库助手，回时会参考检索到的知识文档。")
                            .model(model)
                            .hook(ragHook)
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
                .sessionId("rag-demo").userId("alice").build();
    }

    private static Document doc(String id, String text) {
        return new Document(new DocumentMetadata(io.agentscope.core.message.TextBlock.builder().text(text).build(), id, "chunk-0"));
    }

    /**
     * 内存版 {@link Knowledge}：按关键词匹配检索。
     * 生产环境应替换为向量库实现（Milvus/Qdrant/Pinecone）。
     */
    static class InMemoryKnowledge implements Knowledge {
        private final List<Document> docs = new ArrayList<>();

        @Override
        public reactor.core.publisher.Mono<Void> addDocuments(List<Document> documents) {
            return reactor.core.publisher.Mono.fromRunnable(() -> docs.addAll(documents));
        }

        @Override
        public reactor.core.publisher.Mono<List<Document>> retrieve(String query, RetrieveConfig config) {
            return reactor.core.publisher.Mono.fromSupplier(() -> docs.stream()
                    .filter(d -> {
                        String text = d.getMetadata().getContentText();
                        return text.contains(query) || query.contains(text.substring(0,
                                Math.min(text.length(), 10)));
                    })
                    .limit(config.getLimit())
                    .collect(Collectors.toList()));
        }
    }
}