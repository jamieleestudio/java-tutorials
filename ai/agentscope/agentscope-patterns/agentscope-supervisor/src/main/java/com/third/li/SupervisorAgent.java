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
 * Supervisor 模式（主管编排）。
 *
 * <p>Supervisor（主管）Agent 负责<b>管理多个子 Agent</b>：分配任务、监控进度、
 * 收集结果、做最终决策。子 Agent 之间不直接通信，所有协调都经过主管。
 *
 * <p>与 Embabel 的 {@code embabel-supervisor} 对照——Embabel 用 LLM 当 supervisor
 * 编排动作；AgentScope 用主 Agent + {@link SubagentDeclaration} 声明子 Agent，
 * 通过 Agent-as-Tool 委派。
 *
 * <p>本模块演示 3 个角色：
 * <ul>
 *   <li>supervisor（主管）：理解任务、委派、汇总</li>
 *   <li>research-agent（研究员）：查资料</li>
 *   <li>writing-agent（写手）：整理成文</li>
 * </ul>
 */
@Component
public class SupervisorAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public SupervisorAgent(
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

                    SubagentDeclaration researcher = SubagentDeclaration.builder()
                            .name("research-agent")
                            .description("研究员子 Agent。需要查找资料、收集信息时委派给它。" +
                                    "产出研究要点列表。")
                            .workspaceMode(WorkspaceMode.SHARED)
                            .maxIters(3)
                            .build();

                    SubagentDeclaration writer = SubagentDeclaration.builder()
                            .name("writing-agent")
                            .description("写手子 Agent。需要把要点组织成连贯文章时委派给它。")
                            .workspaceMode(WorkspaceMode.SHARED)
                            .maxIters(3)
                            .build();

                    local = HarnessAgent.builder()
                            .name("supervisor")
                            .sysPrompt("""
                                    你是主管（Supervisor）。你的团队：
                                    - research-agent：研究员，负责查资料收集信息
                                    - writing-agent：写手，负责把要点写成文章

                                    工作流程：
                                    1. 用户提出主题
                                    2. 委派给 research-agent 研究并返回要点
                                    3. 把要点交给 writing-agent 写成文章
                                    4. 返回成稿给用户

                                    只做协调，不要自己写内容。
                                    """)
                            .model(model)
                            .subagent(researcher)
                            .subagent(writer)
                            .workspace(Paths.get(".agentscope/agentscope-supervisor"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("supervisor").userId("alice").build();
    }
}