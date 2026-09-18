package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.subagent.SubagentDeclaration;
import io.agentscope.harness.agent.subagent.WorkspaceMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * Orchestrator-Workers 模式（编排者-工人）。
 *
 * <p>主 Agent（Orchestrator）负责<b>拆解任务、分配工人、收集结果</b>，
 * 多个工人 Agent（Workers）各自执行子任务。
 *
 * <p>与 Embabel 的 `@Action` 动态拆解不同——Embabel 用规划器推导动作序列；
 * AgentScope 用 {@link SubagentDeclaration} 声明工人子 Agent，
 * 主 Agent 通过工具调用（Agent-as-Tool）委派。
 *
 * <p>本模块演示：
 * <ul>
 *   <li>主 Agent（orchestrator）：理解任务 → 拆成子任务 → 委派 → 汇总</li>
 *   <li>工人 Agent（writer）：负责"写代码"子任务</li>
 *   <li>工人 Agent（reviewer）：负责"审查代码"子任务</li>
 * </ul>
 */
@Component
public class OrchestratorWorkersAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public OrchestratorWorkersAgent(
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
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();

                    SubagentDeclaration writer = SubagentDeclaration.builder()
                            .name("writer")
                            .description("编码工人。当需要编写代码实现时委派给它，它会产出完整代码。")
                            .workspaceMode(WorkspaceMode.SHARED)
                            .maxIters(4)
                            .build();

                    SubagentDeclaration reviewer = SubagentDeclaration.builder()
                            .name("reviewer")
                            .description("代码审查工人。当需要审查代码质量时委派给它，它会找出问题并给出改进建议。")
                            .workspaceMode(WorkspaceMode.SHARED)
                            .maxIters(2)
                            .build();

                    local = HarnessAgent.builder()
                            .name("orchestrator")
                            .sysPrompt("""
                                    你是编排者（Orchestrator）。你的职责：
                                    1. 分析用户任务
                                    2. 拆解为子任务（写代码 / 审查代码等）
                                    3. 把子任务委派给对应的工人子 Agent
                                    4. 收集工人结果，汇总成最终交付

                                    你有两个工人子 Agent：
                                    - writer：编写代码
                                    - reviewer：审查代码

                                    对于简单的查询，直接回答即可；对于开发任务，委派给工人。
                                    """)
                            .model(model)
                            .subagent(writer)
                            .subagent(reviewer)
                            .workspace(Paths.get(".agentscope/agentscope-orchestrator-workers"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("orchestrator-workers").userId("alice").build();
    }
}