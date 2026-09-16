package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工具进阶接口。
 *
 * <p>两个端点对应两个 Agent（注意它们的目标类型不同，见 {@link Introspection}）：
 * <ul>
 *   <li>{@code /tools-advanced/ask} —— 渐进式工具 + 循环回调 + 工具名纠正</li>
 *   <li>{@code /tools-advanced/inspect} —— 自省工具（查看黑板与进程状态）</li>
 * </ul>
 */
@RestController
public class ToolsAdvancedController {

    private final AgentPlatform agentPlatform;

    public ToolsAdvancedController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/tools-advanced/ask")
    public Reply ask(
            @RequestParam(value = "message", defaultValue = "帮我看看 A1001 这个订单的状态") String message) {
        return AgentInvocation.create(agentPlatform, Reply.class).invoke(new UserInput(message));
    }

    @GetMapping("/tools-advanced/inspect")
    public Introspection inspect(
            @RequestParam(value = "message", defaultValue = "先查看你当前的黑板与进程状态，再回答：什么是类型化建模？") String message) {
        return AgentInvocation.create(agentPlatform, Introspection.class).invoke(new UserInput(message));
    }
}
