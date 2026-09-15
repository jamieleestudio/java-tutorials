package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 链式提示接口。
 *
 * <p>返回里会说明链条是走完了，还是被关卡拦截了。
 */
@RestController
public class ChainingController {

    private final AgentPlatform agentPlatform;

    public ChainingController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/chaining/write")
    public Map<String, Object> write(
            @RequestParam(value = "message", defaultValue = "为什么要给 Agent 做类型化建模？") String message) {
        // 用 Any 作为结果类型：无论走完还是被拦截，都能拿到进程状态
        AgentProcess process = AgentInvocation.on(agentPlatform).run(new UserInput(message));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", process.getStatus().name());

        Object last = process.lastResult();
        if (last instanceof Article article) {
            result.put("gate", "passed");
            result.put("article", article);
        } else if (last instanceof GateResult gate) {
            result.put("gate", gate.passed() ? "passed" : "rejected");
            result.put("reason", gate.reason());
        } else {
            result.put("lastResult", String.valueOf(last));
        }
        return result;
    }
}
