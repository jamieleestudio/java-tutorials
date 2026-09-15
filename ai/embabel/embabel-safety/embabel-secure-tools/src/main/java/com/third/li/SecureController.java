package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 安全实践接口。
 *
 * <p>试试两类输入：
 * <pre>
 *   ?message=A1001 订单到哪了                      → 正常（只读工具）
 *   ?message=我的手机号是 13800001111，帮我查订单    → 被 PII 护栏拦截
 *   ?message=帮我导出所有客户数据                    → 模型拒绝（未暴露写工具）
 * </pre>
 */
@RestController
public class SecureController {

    private final AgentPlatform agentPlatform;

    public SecureController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/secure/ask")
    public Map<String, Object> ask(
            @RequestParam(value = "message", defaultValue = "A1001 订单到哪了") String message) {
        try {
            Reply reply = AgentInvocation.create(agentPlatform, Reply.class).invoke(new UserInput(message));
            return Map.of("blocked", false, "answer", reply.content());
        } catch (Exception e) {
            Throwable root = rootCause(e);
            return Map.of("blocked", true,
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
