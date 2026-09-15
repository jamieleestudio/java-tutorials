package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MCP 接口。
 *
 * <p>需要本机能执行 {@code docker}（应用会用它拉起 MCP filesystem server）。
 * 沙箱目录见 {@code demo.mcp-root}。
 */
@RestController
public class McpController {

    private final AgentPlatform agentPlatform;

    public McpController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/mcp/ask")
    public ChatReply ask(
            @RequestParam(value = "message",
                    defaultValue = "列出沙箱目录里的文件，读取 readme.md 并用一句话总结") String message) {
        return AgentInvocation.create(agentPlatform, ChatReply.class).invoke(new UserInput(message));
    }
}
