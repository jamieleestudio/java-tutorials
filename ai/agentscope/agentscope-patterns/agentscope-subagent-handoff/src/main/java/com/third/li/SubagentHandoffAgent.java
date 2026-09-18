package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.subagent.SubagentDeclaration;
import io.agentscope.harness.agent.subagent.WorkspaceMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * Subagent Handoff 模式（子 Agent 委派）。
 *
 * <p>把任务<b>转交（handoff）</b>给专业化子 Agent 处理，与 orchestrator-workers
 * 的区别：handoff 是"全权转交"（主 Agent 不参与执行），而 orchestrator 会
 * 拆解+汇总。
 *
 * <p>与 Embabel 的 {@code embabel-subagent} 对照——Embabel 用 SubagentProcess
 * 进程内嵌套委派；AgentScope 用 {@link SubagentDeclaration} + Agent-as-Tool。
 *
 * <p>本模块演示：主 Agent（前台）遇到翻译需求时，把整件事 handoff 给
 * 专业翻译子 Agent（translator），自己不参与。
 */
@Component
public class SubagentHandoffAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public SubagentHandoffAgent(
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

                    SubagentDeclaration translator = SubagentDeclaration.builder()
                            .name("translator")
                            .description("专业英译中翻译子 Agent。当需要翻译英文文本为中文时，" +
                                    "把任务完整交给它，它返回翻译结果。")
                            .workspaceMode(WorkspaceMode.SHARED)
                            .maxIters(2)
                            .build();

                    local = HarnessAgent.builder()
                            .name("front-desk")
                            .sysPrompt("""
                                    你是前台助手。你能处理日常查询。
                                    当用户需要翻译英文为中文时，把整个任务转交给 translator 子 Agent，
                                    并原样返回它的翻译结果，不要自行修改。
                                    其他问题直接回答。
                                    """)
                            .model(model)
                            .subagent(translator)
                            .workspace(Paths.get(".agentscope/agentscope-subagent-handoff"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("subagent-handoff").userId("alice").build();
    }
}