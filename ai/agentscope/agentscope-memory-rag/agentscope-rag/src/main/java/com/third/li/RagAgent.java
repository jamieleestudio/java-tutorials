package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.rag.GenericRAGHook;
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
import java.util.List;

/**
 * RAG 检索增强生成（Knowledge + KnowledgeRetrievalTools + GenericRAGHook）。
 *
 * <p>AgentScope 的 RAG 系统分两种模式（{@code RAGMode}）：
 * <ul>
 *   <li>{@code GENERIC} — 通过 Hook 自动检索，注入到系统提示</li>
 *   <li>{@code AGENTIC} — 通过工具让 Agent 主动检索</li>
 * </ul>
 *
 * <p>核心组件：
 * <ul>
 *   <li>{@link Knowledge} — 知识库接口：addDocuments / retrieve</li>
 *   <li>{@link KnowledgeRetrievalTools} — 工具：retrieveKnowledge</li>
 *   <li>{@link GenericRAGHook} — Hook：自动检索并注入</li>
 * </ul>
 *
 * <p>本模块演示 AGENTIC 模式：Agent 通过工具主动检索知识库。
 */
@Component
public class RagAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public RagAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENI_API_KEY:}}") String apiKey,
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
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();

                    Knowledge knowledge = new InMemoryKnowledge();
                    KnowledgeRetrievalTools ragTools = new KnowledgeRetrievalTools(knowledge);
                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(ragTools);

                    local = HarnessAgent.builder()
                            .name("rag-agent")
                            .sysPrompt("你是一个知识问答助手。你可以使用 retrieveKnowledge 工具" +
                                    "检索知识库获取相关信息，然后基于检索结果回答问题。")
                            .model(model)
                            .toolkit(toolkit)
                            .workspace(Paths.get(".agentscope/workspace-rag"))
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

    /**
     * 简单的内存 Knowledge 实现——关键词匹配。
     * 生产环境应替换为向量数据库（Qdrant、Milvus 等）。
     */
    static class InMemoryKnowledge implements Knowledge {
        private final List<Document> docs = new java.util.ArrayList<>();

        InMemoryKnowledge() {
            docs.add(createDoc("doc1", "Spring Boot 自动配置原理"));
            docs.add(createDoc("doc2", "AgentScope 中间件设计"));
            docs.add(createDoc("doc3", "RAG 检索增强生成最佳实践"));
        }

        private Document createDoc(String id, String title) {
            var content = io.agentscope.core.message.TextBlock.builder().text(title).build();
            var meta = DocumentMetadata.builder()
                    .docId(id).chunkId(id).content(content).build();
            return new Document(meta);
        }

        @Override
        public reactor.core.publisher.Mono<Void> addDocuments(List<Document> documents) {
            docs.addAll(documents);
            return reactor.core.publisher.Mono.empty();
        }

        @Override
        public reactor.core.publisher.Mono<List<Document>> retrieve(String query, RetrieveConfig config) {
            List<Document> results = docs.stream()
                    .filter(d -> {
                        String text = d.getMetadata().getContentText();
                        return text != null && text.contains(query);
                    })
                    .limit(config.getLimit())
                    .collect(java.util.stream.Collectors.toList());
            return reactor.core.publisher.Mono.just(results);
        }
    }
}