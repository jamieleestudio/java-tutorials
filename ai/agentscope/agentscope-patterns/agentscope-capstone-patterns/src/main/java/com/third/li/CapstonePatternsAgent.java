package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystemWithShell;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.subagent.SubagentDeclaration;
import io.agentscope.harness.agent.subagent.WorkspaceMode;
import io.agentscope.harness.agent.tool.FilesystemTool;
import io.agentscope.harness.agent.tool.ShellExecuteTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * Patterns Capstone 模式（端到端综合）。
 *
 * <p>把前 18 个模式的核心要素整合进一个<b>完整 Agent 系统</b>：
 * <ul>
 *   <li><b>ReAct 自主循环</b>（autonomous-agent）：内置 Shell + Filesystem 工具</li>
 *   <li><b>工具链</b>（tool-chaining）：先 createProject 再操作</li>
 *   <li><b>子 Agent 委派</b>（subagent-handoff）：复杂任务交给 review 子 Agent</li>
 *   <li><b>权限</b>（permission）：ACCEPT_EDITS 自动接受编辑</li>
 *   <li><b>上下文压缩</b>（middleware）：长对话自动摘要</li>
 *   <li><b>预算</b>（budget）：迭代上限防失控</li>
 * </ul>
 *
 * <p>与 Embabel 的 {@code embabel-capstone} 对照——Embabel 把护栏/政策/工具/HITL/
 * 预算/观测串成一个系统；本模块用 AgentScope 的 Builder 链一次性装配。
 */
@Component
public class CapstonePatternsAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public CapstonePatternsAgent(
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

                    var wsPath = Paths.get(".agentscope/agentscope-capstone-patterns").toAbsolutePath();
                    wsPath.toFile().mkdirs();
                    LocalFilesystemWithShell fs = new LocalFilesystemWithShell(wsPath);
                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(new ShellExecuteTool(fs));
                    toolkit.registerTool(new FilesystemTool(fs));

                    SubagentDeclaration reviewer = SubagentDeclaration.builder()
                            .name("reviewer")
                            .description("代码审查子 Agent。当需要审查产出质量时委派给它。")
                            .workspaceMode(WorkspaceMode.SHARED)
                            .maxIters(2)
                            .build();

                    CompactionConfig compaction = CompactionConfig.builder()
                            .triggerMessages(10)
                            .triggerTokens(4000)
                            .keepMessages(5)
                            .build();

                    PermissionContextState perm = PermissionContextState.builder()
                            .mode(PermissionMode.ACCEPT_EDITS)
                            .build();

                    local = HarnessAgent.builder()
                            .name("pattern-capstone")
                            .sysPrompt("""
                                    你是全功能开发 Agent。你能：
                                    1. 读写文件 + 执行 shell 命令（自主 ReAct）
                                    2. 创建项目并管理任务（工具链）
                                    3. 需要质量审查时委派给 reviewer 子 Agent
                                    请自主完成用户任务。
                                    """)
                            .model(model)
                            .toolkit(toolkit)
                            .subagent(reviewer)
                            .compaction(compaction)
                            .permissionContext(perm)
                            .maxIters(15)
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
                .sessionId("pattern-capstone").userId("alice").build();
    }
}