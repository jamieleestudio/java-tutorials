package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.permission.PermissionBehavior;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.permission.PermissionRule;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.builtin.TodoTools;
import io.agentscope.core.tool.coding.ShellCommandTool;
import io.agentscope.core.tool.file.ReadFileTool;
import io.agentscope.core.tool.file.WriteFileTool;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Set;

/**
 * 编码 Agent（Claude Code 式）—— 综合演示：编码工具 + 计划模式 + 任务列表 + 权限规则。
 *
 * <p>把前面模块的能力合成一个完整的编码 Agent：
 * <ul>
 *   <li><b>编码工具</b>：ShellCommandTool + ReadFileTool + WriteFileTool + TodoTools</li>
 *   <li><b>计划模式</b>：{@code enablePlanMode()} —— 先规划再执行</li>
 *   <li><b>任务列表</b>：{@code enableTaskList()} —— 跟踪后台任务</li>
 *   <li><b>权限规则</b>：删除操作 DENY、写操作 ASK —— 危险操作需确认</li>
 *   <li><b>上下文压缩</b>：长对话自动压缩</li>
 * </ul>
 *
 * <p>这是 AgentScope 工程化能力的综合展示：一个 Agent 同时具备工具调用、
 * 权限管控、任务编排、计划规划、记忆管理——类似 Claude Code 的工作方式。
 */
@Component
public class CodingCapstoneAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-coding-agent";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public CodingCapstoneAgent(
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
                    Toolkit toolkit = new Toolkit();
                    toolkit.registerAgentTool(new ShellCommandTool(
                            WORKSPACE_DIR,
                            Set.of("ls", "cat", "echo", "pwd", "grep", "find", "wc", "head", "tail", "mkdir", "git", "javac", "java"),
                            cmd -> true));
                    toolkit.registerTool(new ReadFileTool(WORKSPACE_DIR));
                    toolkit.registerTool(new WriteFileTool(WORKSPACE_DIR));
                    toolkit.registerTool(new TodoTools());

                    PermissionRule denyDelete = new PermissionRule(
                            "ShellCommandTool", "执行 rm 或删除文件", PermissionBehavior.DENY, "coding-agent");
                    PermissionRule askWrite = new PermissionRule(
                            "WriteFileTool", "写入或覆盖文件", PermissionBehavior.ASK, "coding-agent");
                    PermissionContextState permCtx = PermissionContextState.builder()
                            .mode(PermissionMode.DEFAULT)
                            .addDenyRule("ShellCommandTool", denyDelete)
                            .addAskRule("WriteFileTool", askWrite)
                            .build();

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("coding-agent")
                            .sysPrompt("你是一个 Claude Code 式编码助手。"
                                    + "先制定计划，再执行；可读写文件、执行安全命令、管理 TODO。"
                                    + "删除操作被禁止，写文件需确认。")
                            .model(model)
                            .toolkit(toolkit)
                            .permissionContext(permCtx)
                            .stopOnReject(true)
                            .enablePlanMode()
                            .planFileDirectory(".agentscope/coding-agent-plans")
                            .enableTaskList()
                            .compaction(io.agentscope.harness.agent.memory.compaction.CompactionConfig.builder()
                                    .triggerTokens(4000)
                                    .keepMessages(6)
                                    .build())
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
                .sessionId("coding-capstone-demo").userId("alice").build();
    }
}