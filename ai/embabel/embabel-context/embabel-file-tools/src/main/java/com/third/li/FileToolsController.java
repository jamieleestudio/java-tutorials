package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件工具接口。
 *
 * <p>沙箱目录见 {@code demo.sandbox-dir}（默认在系统临时目录下）。
 */
@RestController
public class FileToolsController {

    private final AgentPlatform agentPlatform;

    public FileToolsController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/files/ask")
    public ChatReply ask(
            @RequestParam(value = "message",
                    defaultValue = "列出沙箱目录里的文件，读取 notes.md 并用一句话总结它的内容") String message) {
        return AgentInvocation.create(agentPlatform, ChatReply.class).invoke(new UserInput(message));
    }
}
