package com.third.li;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.EditFileTool;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.GrepTool;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.ListFilesTool;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.ReadFileTool;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.WriteFileTool;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 内置文件工具套件：SAA 自带的"类 Claude Code 文件操作"。
 *
 * <p>agent-framework 内置了一整套文件系统工具（对应 {@code FilesystemInterceptor} /
 * {@code LocalFilesystemBackend} 的能力），每个工具都提供
 * {@code createXxxToolCallback(baseDir)} 静态工厂，把操作范围锁定在指定目录内：
 * <ul>
 *   <li>{@link ReadFileTool} / {@link WriteFileTool} / {@link EditFileTool} — 读/写/精确替换编辑</li>
 *   <li>{@link GrepTool}（内容搜索）；GlobTool/ListFilesTool 在 M1.1 的 schema 有缺陷暂不注册</li>
 * </ul>
 *
 * <p>这与 {@code ai/agentscope} 的 FilesystemTool、Claude Code 的文件工具是同一范式，
 * 是 SAA 构建 coding agent 的地基。
 */
@Service
public class AgentFilesystemService {

    private final ReactAgent agent;
    private final Path workspace;

    public AgentFilesystemService(ChatModel chatModel) throws GraphStateException, IOException {
        // 演示工作区：启动时预置两个文件，工具的操作范围锁定在该目录
        this.workspace = Path.of("demo-workspace").toAbsolutePath();
        Files.createDirectories(workspace);
        if (Files.notExists(workspace.resolve("notes.md"))) {
            Files.writeString(workspace.resolve("notes.md"),
                    "# 学习笔记\n\n1. Spring AI Alibaba 的 Graph 源自 LangGraph 思想。\n");
        }
        if (Files.notExists(workspace.resolve("todo.txt"))) {
            Files.writeString(workspace.resolve("todo.txt"), "整理 Graph 教程大纲\n");
        }

        String baseDir = workspace.toString();
        // 注：M1.1 的 GlobTool / ListFilesTool 生成的 JSON Schema 有缺陷（DeepSeek 校验拒绝），暂不注册
        List<ToolCallback> fsTools = List.of(
                ReadFileTool.createReadFileToolCallback(baseDir),
                WriteFileTool.createWriteFileToolCallback(baseDir),
                EditFileTool.createEditFileToolCallback(baseDir),
                GrepTool.createGrepToolCallback(baseDir));

        this.agent = ReactAgent.builder()
                .name("fs-agent")
                .description("文件管理助手")
                .systemPrompt("你是文件管理助手。所有文件操作只能在工作目录内进行。"
                        + "操作完成后简要汇报做了什么。")
                .model(chatModel)
                .tools(fsTools)
                .build();
    }

    public String ask(String message) throws Exception {
        return agent.call(message).getText();
    }

    /** 列出工作区当前内容，便于观察 agent 的改动。 */
    public String workspaceSnapshot() throws IOException {
        StringBuilder sb = new StringBuilder("工作区 " + workspace + "：\n");
        try (var files = Files.list(workspace)) {
            files.forEach(p -> {
                try {
                    sb.append("- ").append(p.getFileName()).append("：")
                            .append(Files.readString(p).lines().count()).append(" 行\n");
                } catch (IOException ignored) {
                }
            });
        }
        return sb.toString();
    }
}
