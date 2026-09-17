package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystemWithShell;
import io.agentscope.harness.agent.memory.MemoryConfig;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.tool.FilesystemTool;
import io.agentscope.harness.agent.tool.ShellExecuteTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 总结项目 1：编码 Agent。
 *
 * <p>整合前 7 个分类的 API：
 * <ul>
 *   <li>basics — HarnessAgent builder + OpenAIChatModel</li>
 *   <li>middleware — CompactionMiddleware（通过 .compaction()）</li>
 *   <li>tools-skills — ShellExecuteTool + FilesystemTool（内置编码工具）</li>
 *   <li>permission — PermissionMode.ACCEPT_EDITS（自动接受编辑）</li>
 *   <li>workspace — LocalFilesystemWithShell（本地工作区 + shell）</li>
 *   <li>memory — MemoryConfig（对话记忆 + flush）</li>
 *   <li>service — 通过 REST controller 调用</li>
 * </ul>
 *
 * <p>这个 Agent 可以：
 * <ol>
 *   <li>读写文件（FilesystemTool）</li>
 *   <li>执行命令（ShellExecuteTool）</li>
 *   <li>自动压缩长对话（CompactionMiddleware）</li>
 *   <li>记忆跨会话信息（MemoryFlush）</li>
 *   <li>自动接受编辑操作（ACCEPT_EDITS 模式）</li>
 * </ol>
 */
@Component
public class CodingCapstoneAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public CodingCapstoneAgent(
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

                    var wsPath = Paths.get(".agentscope/workspace-coding-agent").toAbsolutePath();
                    wsPath.toFile().mkdirs();
                    LocalFilesystemWithShell fs = new LocalFilesystemWithShell(wsPath);
                    ShellExecuteTool shellTool = new ShellExecuteTool(fs);
                    FilesystemTool fileTool = new FilesystemTool(fs);
                    var toolkit = new io.agentscope.core.tool.Toolkit();
                    toolkit.registerTool(shellTool);
                    toolkit.registerTool(fileTool);

                    CompactionConfig compaction = CompactionConfig.builder()
                            .triggerMessages(10)
                            .triggerTokens(4000)
                            .keepMessages(5)
                            .keepTokensRatio(0.3)
                            .build();

                    MemoryConfig memory = MemoryConfig.builder()
                            .model(model)
                            .sessionRetentionDays(30)
                            .build();

                    PermissionContextState perm = PermissionContextState.builder()
                            .mode(PermissionMode.ACCEPT_EDITS)
                            .build();

                    local = HarnessAgent.builder()
                            .name("coding-capstone")
                            .sysPrompt("你是一个全功能编码 Agent。你可以：\n" +
                                    "1. 读写文件（readFile, writeFile, editFile）\n" +
                                    "2. 执行 shell 命令（shellExecute）\n" +
                                    "3. 搜索文件（grep, glob）\n" +
                                    "请根据用户需求完成编码任务。")
                            .model(model)
                            .toolkit(toolkit)
                            .compaction(compaction)
                            .memory(memory)
                            .permissionContext(perm)
                            .maxIters(20)
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
                .sessionId("capstone-coding").userId("alice").build();
    }
}