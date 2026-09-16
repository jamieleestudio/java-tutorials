package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自省工具接口。
 *
 * <p>试试让它先自省再回答：
 * <pre>
 *   ?message=我现在掌握哪些信息？你打算怎么回答"什么是类型化建模"？
 * </pre>
 */
@RestController
public class AgenticToolsController {

    private final AgentPlatform agentPlatform;

    public AgenticToolsController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/agentic-tools/ask")
    public Reply ask(
            @RequestParam(value = "message", defaultValue = "先查看你当前的黑板与进程状态，再回答：什么是类型化建模？") String message) {
        return AgentInvocation.create(agentPlatform, Reply.class).invoke(new UserInput(message));
    }
}
