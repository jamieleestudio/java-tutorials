package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.KnowledgeRetrievalTools;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.model.DocumentMetadata;
import io.agentscope.core.rag.model.RetrieveConfig;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.MemoryConfig;
import io.agentscope.harness.agent.subagent.SubagentDeclaration;
import io.agentscope.harness.agent.subagent.WorkspaceMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 总结项目 2：端到端多 Agent 系统。
 *
 * <p>整合所有分类的 API，构建一个完整的智能助手平台：
 * <ul>
 *   <li>主 Agent — 协调员，接收用户请求，委派给子 Agent</li>
 *   <li>子 Agent 1 (researcher) — 研究员，使用 RAG 检索知识库</li>
 *   <li>子 Agent 2 (writer) — 写作员，根据研究结果生成报告</li>
 *   <li>记忆系统 — 跨会话记忆 + 上下文压缩</li>
 *   <li>Gateway — 统一入口（通过 REST controller）</li>
 * </ul>
 *
 * <p>工作流：
 * <ol>
 *   <li>用户提问 → 主 Agent 接收</li>
 *   <li>主 Agent 委派给 researcher 子 Agent</li>
 *   <li>researcher 用 RAG 检索知识库，返回研究结果</li>
 *   <li>主 Agent 委派给 writer 子 Agent</li>
 *   <li>writer 根据研究结果生成报告</li>
 *   <li>主 Agent 返回最终报告</li>
 * </ol>
 */
@Component
public class E2ECapstoneAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public E2ECapstoneAgent(
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

                    SubagentDeclaration researcher = SubagentDeclaration.builder()
                            .name("researcher")
                            .description("研究子 Agent。当需要查找资料时委派给它。" +
                                    "它会检索知识库并返回研究结果。")
                            .workspaceMode(WorkspaceMode.SHARED)
                            .maxIters(3)
                            .build();

                    SubagentDeclaration writer = SubagentDeclaration.builder()
                            .name("writer")
                            .description("写作子 Agent。当需要生成报告或文档时委派给它。" +
                                    "它会根据提供的研究结果撰写内容。")
                            .workspaceMode(WorkspaceMode.SHARED)
                            .maxIters(3)
                            .build();

                    local = HarnessAgent.builder()
                            .name("e2e-coordinator")
                            .sysPrompt("""
                                    你是一个智能助手平台的协调员。你有两个子 Agent：
                                    1. researcher — 检索知识库获取信息
                                    2. writer — 根据信息生成报告

                                    工作流程：
                                    - 用户提问后，先委派给 researcher 查找资料
                                    - 拿到研究结果后，委派给 writer 生成报告
                                    - 把最终报告返回给用户
                                    """)
                            .model(model)
                            .subagent(researcher)
                            .subagent(writer)
                            .memory(MemoryConfig.builder().model(model).build())
                            .workspace(Paths.get(".agentscope/workspace-capstone-e2e"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("capstone-e2e").userId("alice").build();
    }
}