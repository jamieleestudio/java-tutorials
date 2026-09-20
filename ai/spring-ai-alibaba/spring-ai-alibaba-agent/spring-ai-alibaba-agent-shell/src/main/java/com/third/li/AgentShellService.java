package com.third.li;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool2;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/**
 * Shell 命令执行工具：ShellTool2 + ShellToolAgentHook。
 *
 * <p>{@link ShellTool2} 是 SAA 内置的 shell 执行工具（支持会话、超时、输出截断），
 * 通过 {@link ShellToolAgentHook}（实现 {@code ToolInjection}）注入 Agent：
 * hook 会把工具挂到 Agent 的工具列表里，并在系统提示中说明其用法。
 *
 * <p>安全提示：生产环境务必把工作目录限定在沙箱/容器内
 * （SAA 另有 spring-ai-alibaba-sandbox 提供远程沙箱执行）。
 */
@Service
public class AgentShellService {

    private final ReactAgent agent;

    public AgentShellService(ChatModel chatModel) throws GraphStateException {
        // shell 会话工作目录限定在 demo-workspace，超时 10 秒
        ShellTool2 shellTool = ShellTool2.builder(Path.of("demo-workspace").toAbsolutePath().toString())
                .withCommandTimeout(10_000)
                .withMaxOutputLines(50)
                .build();

        this.agent = ReactAgent.builder()
                .name("shell-agent")
                .description("Shell 运维助手")
                .systemPrompt("你是 shell 助手。需要查看目录、读文件、统计信息时调用 shell 工具执行命令，"
                        + "命令尽量简单安全（禁止 rm -rf 等破坏性命令）。回答里带上命令输出摘要。")
                .model(chatModel)
                .hooks(ShellToolAgentHook.builder()
                        .shellTool2(shellTool)
                        .build())
                .build();
    }

    public String ask(String message) throws Exception {
        return agent.call(message).getText();
    }

    /** 可用的 shell 演示命令提示。 */
    public List<String> hints() {
        return List.of(
                "列出当前目录的文件：ls",
                "统计 notes.md 的行数",
                "查看系统时间");
    }
}
