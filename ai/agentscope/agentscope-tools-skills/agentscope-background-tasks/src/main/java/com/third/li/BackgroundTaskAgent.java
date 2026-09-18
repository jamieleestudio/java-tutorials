package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 后台任务 Agent：通过 {@code HarnessAgent.Builder.enableTaskList()} 开启任务列表工具，
 * 让 Agent 能把长任务移到后台、跟踪任务状态、在完成后唤醒自己。
 *
 * <p>AgentScope 的后台任务能力由两层组成：
 * <ul>
 *   <li>{@code enableTaskList()} —— 在 Toolkit 里注册内置的 TaskTool，
 *       Agent 可创建/查询/取消任务（状态写入 {@link io.agentscope.core.state.AgentState}）</li>
 *   <li>{@code asyncToolTimeout(Duration)} —— 异步工具的超时阈值，
 *       超过则工具转入后台，Agent 不阻塞等待</li>
 * </ul>
 *
 * <p>对应底层是 {@link io.agentscope.harness.agent.subagent.task.TaskRepository}
 * （由 HarnessAgent 自动注入），持久化任务记录到工作区。
 */
@Component
public class BackgroundTaskAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-background-tasks";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public BackgroundTaskAgent(
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
                    local = HarnessAgent.builder()
                            .name("background-tasks")
                            .sysPrompt("你是一个任务管理助手，可以把耗时任务移到后台执行，"
                                    + "用 task 工具创建/查询/取消任务，完成后唤醒自己汇报结果。")
                            .model(model)
                            .enableTaskList()
                            .asyncToolTimeout(java.time.Duration.ofSeconds(30))
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
                .sessionId("background-demo").userId("alice").build();
    }
}