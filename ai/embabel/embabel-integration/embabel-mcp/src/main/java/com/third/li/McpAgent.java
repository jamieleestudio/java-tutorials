package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * MCP 集成示例。
 *
 * <p>MCP（Model Context Protocol）用统一协议暴露外部工具。这里的用法：
 * <ol>
 *   <li>在 {@code application.yml} 里用 Spring AI 的
 *       {@code spring.ai.mcp.client.stdio.connections.filesystem} 拉起 MCP server（docker + stdio）</li>
 *   <li>用 {@code embabel.agent.platform.tools.includes.files} 把该 server 的工具挂成一个**工具组**</li>
 *   <li>动作里 {@code withToolGroup("files")} 请求该工具组，模型即可调用这些工具</li>
 * </ol>
 *
 * <p>工具是**惰性加载**的：第一次用到时才与 MCP server 握手（listTools），
 * 因此启动阶段不会阻塞。
 */
@Agent(description = "MCP 示例：通过 MCP filesystem server 操作沙箱文件")
public class McpAgent {

    @Action(description = "使用 MCP 文件工具完成用户请求")
    @AchievesGoal(description = "返回操作结果")
    public ChatReply chat(UserInput userInput, Ai ai) {
        String answer = ai.withDefaultLlm()
                .withToolGroup("files")
                .withId("mcp-files")
                .generateText("""
                        你可以使用 MCP 提供的文件工具（工具名形如 read_text_file、list_directory、write_file 等）。
                        所有路径都相对于沙箱根目录（例如 "readme.md"）。
                        请先了解现状再执行用户请求，最后简要说明做了什么。

                        用户请求：%s
                        """.formatted(userInput.getContent()));
        return new ChatReply(answer);
    }
}
