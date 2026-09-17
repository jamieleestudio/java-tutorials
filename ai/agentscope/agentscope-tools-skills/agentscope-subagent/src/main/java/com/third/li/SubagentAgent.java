package com.third.li;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.subagent.SubAgentConfig;
import io.agentscope.core.tool.subagent.SubAgentProvider;
import io.agentscope.core.tool.subagent.SubAgentTool;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.subagent.SubagentDeclaration;
import io.agentscope.harness.agent.subagent.WorkspaceMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 子代理（SubAgentTool + SubagentDeclaration）。
 *
 * <p>AgentScope 的子代理机制把另一个 Agent 包装成<b>工具</b>，
 * 让主 Agent 可以委派子任务给专业化的子 Agent。
 *
 * <p>两种方式：
 * <ol>
 *   <li><b>声明式</b>：通过 {@link HarnessAgent.Builder#subagent(SubagentDeclaration)}
 *       声明子 Agent 的配置（name, description, model, maxIters），框架自动创建</li>
 *   <li><b>编程式</b>：通过 {@link SubAgentTool} 直接包装一个 {@link SubAgentProvider}</li>
 * </ol>
 *
 * <p>本模块演示声明式（推荐）：主 Agent 是"翻译协调员"，
 * 子 Agent 是"专业翻译"（English→Chinese）。
 *
 * <p>与 Embabel 的差异：Embabel 没有"子 Agent"概念——
 * 它通过 @Action 委派给其他 Process，但不是"把 Agent 当工具"。
 * AgentScope 的 SubAgentTool 是"Agent-as-Tool"模式，
 * 类似 Claude Code 的 subagent。
 */
@Component
public class SubagentAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public SubagentAgent(
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

                    SubagentDeclaration translator = SubagentDeclaration.builder()
                            .name("translator")
                            .description("专业英译中翻译子 Agent。当需要翻译英文文本为中文时委派给它。")
                            .workspaceMode(WorkspaceMode.SHARED)
                            .maxIters(2)
                            .build();

                    local = HarnessAgent.builder()
                            .name("subagent-coordinator")
                            .sysPrompt("你是一个翻译协调员。当用户需要翻译英文为中文时，" +
                                    "委派给 translator 子 Agent 执行翻译。对于其他问题，直接回答。")
                            .model(model)
                            .subagent(translator)
                            .workspace(Paths.get(".agentscope/workspace-subagent"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("subagent-demo").userId("alice").build();
    }
}