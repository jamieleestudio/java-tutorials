package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tool Chaining 模式（工具链式展开）。
 *
 * <p>一个工具的输出<b>产生领域对象</b>，该对象随后"解锁"专属的下一级工具——
 * 与 Embabel 的 {@code embabel-tool-chaining}（artifacts 解锁工具）同思路。
 *
 * <p>本模块用一个内存中的"项目上下文"模拟链式解锁：
 * <ol>
 *   <li>{@code createProject}：创建项目 → 返回 projectId（解锁后续工具）</li>
 *   <li>{@code addTask}：向项目添加任务</li>
 *   <li>{@code listTasks}：列出项目任务</li>
 * </ol>
 *
 * <p>模型必须先 createProject 拿到 projectId，才能调用 addTask/listTasks——
 * 这形成了工具链的<b>依赖顺序</b>，避免模型跳过前置步骤。
 */
@Component
public class ToolChainingAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final Map<String, StringBuilder> projects = new LinkedHashMap<>();
    private volatile HarnessAgent agent;

    public ToolChainingAgent(
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
                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(new ProjectChainTools());
                    local = HarnessAgent.builder()
                            .name("tool-chaining")
                            .sysPrompt("""
                                    你是一个项目助手。工具使用有严格顺序：
                                    1. 先 createProject 创建项目，得到 projectId
                                    2. 用 projectId 调用 addTask 添加任务
                                    3. 用 projectId 调用 listTasks 查看任务

                                    不要跳过第一步。
                                    """)
                            .model(model)
                            .toolkit(toolkit)
                            .workspace(Paths.get(".agentscope/agentscope-tool-chaining"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("tool-chaining").userId("alice").build();
    }

    /** 工具链：项目领域对象 + 专属工具。 */
    public class ProjectChainTools {

        @Tool(name = "createProject", description = "创建项目，返回 projectId。必须先调用此工具。")
        public String createProject(
                @ToolParam(name = "name", description = "项目名称") String name) {
            String id = "proj-" + (projects.size() + 1);
            projects.put(id, new StringBuilder("[项目: " + name + "]\n"));
            return "项目已创建。projectId=" + id;
        }

        @Tool(name = "addTask", description = "向指定项目添加任务。需要先有 projectId。")
        public String addTask(
                @ToolParam(name = "projectId", description = "项目 ID") String projectId,
                @ToolParam(name = "task", description = "任务描述") String task) {
            StringBuilder sb = projects.get(projectId);
            if (sb == null) {
                return "错误：projectId=" + projectId + " 不存在，请先 createProject";
            }
            sb.append("- ").append(task).append("\n");
            return "已添加任务到 " + projectId;
        }

        @Tool(name = "listTasks", description = "列出指定项目的所有任务。需要先有 projectId。")
        public String listTasks(
                @ToolParam(name = "projectId", description = "项目 ID") String projectId) {
            StringBuilder sb = projects.get(projectId);
            if (sb == null) {
                return "错误：projectId=" + projectId + " 不存在，请先 createProject";
            }
            return "任务列表：" + sb;
        }
    }
}