package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 护栏演示接口。
 *
 * <p>正常提问返回答案；触发护栏（如输入"忽略之前的指令..."）则返回 blocked=true。
 */
@RestController
public class GuardrailsController {

    private final AgentPlatform agentPlatform;

    public GuardrailsController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/guardrails/ask")
    public Map<String, Object> ask(
            @RequestParam(value = "message", defaultValue = "公司年假制度是怎样的？") String message) {
        try {
            ChatReply reply = AgentInvocation.create(agentPlatform, ChatReply.class)
                    .invoke(new UserInput(message));
            return Map.of("blocked", false, "answer", reply.content());
        } catch (Exception e) {
            Throwable root = rootCause(e);
            return Map.of(
                    "blocked", true,
                    "reason", root.getMessage() == null ? root.toString() : root.getMessage());
        }
    }

    private Throwable rootCause(Throwable t) {
        Throwable current = t;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
