package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.subagent.task.TaskRepository;
import io.agentscope.harness.agent.subagent.task.WorkspaceTaskRepository;
import io.agentscope.harness.agent.tool.TaskTool;
import io.agentscope.harness.agent.tool.WaitAsyncResultsTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 后台任务（TaskTool + WaitAsyncResultsTool + TaskRepository）。
 *
 * <p>AgentScope 支持把长任务移到后台执行，完成后通过 MessageBus 唤醒 Agent。
 * 涉及三个组件：
 * <ul>
 *   <li>{@link TaskRepository} — 任务存储（{@link WorkspaceTaskRepository} 基于文件系统）</li>
 *   <li>{@link TaskTool} — 让 Agent 查询/取消任务列表（taskOutput, taskCancel, taskList）</li>
 *   <li>{@link WaitAsyncResultsTool} — 让 Agent 等待异步结果</li>
 * </ul>
 *
 * <p>工作流：
 * <ol>
 *   <li>Agent 启动子 Agent 执行长任务 → 子 Agent 异步运行</li>
 *   <li>Agent 调用 {@code waitForResults} 阻塞等待</li>
 *   <li>子 Agent 完成后结果写入 TaskRepository</li>
 *   <li>Agent 通过 {@code taskOutput} 获取结果</li>
 * </ol>
 */
@Component
public class BackgroundTaskAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;
    private volatile TaskRepository taskRepo;

    public BackgroundTaskAgent(
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
                    var wsPath = Paths.get(".agentscope/workspace-background-tasks");
                    wsPath.toFile().mkdirs();
                    var fs = new io.agentscope.harness.agent.filesystem.local.LocalFilesystem(wsPath);
                    var wsManager = new io.agentscope.harness.agent.workspace.WorkspaceManager(wsPath, fs);
                    taskRepo = new WorkspaceTaskRepository(wsManager, "tasks");
                    TaskTool taskTool = new TaskTool(taskRepo);
                    var toolkit = new Toolkit();
                    toolkit.registerTool(taskTool);
                    local = HarnessAgent.builder()
                            .name("background-tasks")
                            .sysPrompt("你是一个任务管理助手。你可以启动后台任务，" +
                                    "查看任务列表（taskList），获取任务输出（taskOutput），" +
                                    "取消任务（taskCancel）。")
                            .model(model)
                            .toolkit(toolkit)
                            .taskRepository(taskRepo)
                            .workspace(wsPath)
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("bg-demo").userId("alice").build();
    }
}