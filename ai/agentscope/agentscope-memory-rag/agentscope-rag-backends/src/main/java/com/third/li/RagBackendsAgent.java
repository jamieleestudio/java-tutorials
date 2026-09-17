package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.model.RetrieveConfig;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;

/**
 * RAG 后端选择（InMemory / Qdrant / Milvus / Chroma）。
 *
 * <p>AgentScope 的 {@link Knowledge} 接口有多种后端实现：
 * <ul>
 *   <li><b>InMemory</b> — 开发/测试用，无持久化</li>
 *   <li><b>Qdrant</b> — 生产级向量数据库（推荐）</li>
 *   <li><b>Milvus</b> — 大规模向量数据库</li>
 *   <li><b>Chroma</b> — 轻量级向量数据库</li>
 * </ul>
 *
 * <p>所有后端实现相同的 {@link Knowledge} 接口，切换只需改构造。
 * 本模块演示 InMemory 实现 + 描述如何切换到 Qdrant。
 */
@Component
public class RagBackendsAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public RagBackendsAgent(
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

    public String describeBackends() {
        return """
                RAG 后端选择：
                1. InMemory（开发）——无持久化，重启丢失
                   new InMemoryKnowledge()
                2. Qdrant（推荐）——Docker 部署，高性能
                   // 需要 agentscope-qdrant 依赖
                   new QdrantKnowledge(host, port, collectionName)
                3. Milvus（大规模）——适合亿级向量
                   new MilvusKnowledge(host, port)
                4. Chroma（轻量）——适合小型项目
                   new ChromaKnowledge(path)

                切换只需改 Knowledge 构造，接口不变。
                """;
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("rag-backends")
                            .sysPrompt("你是一个知识问答助手。")
                            .model(model)
                            .workspace(Paths.get(".agentscope/workspace-rag-backends"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("rag-backends-demo").userId("alice").build();
    }
}