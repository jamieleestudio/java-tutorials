package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 思维树接口。
 *
 * <p>注意：一次搜索约 10 次 LLM 调用，耗时较长（30~60s）。
 */
@RestController
public class TotController {

    private final AgentPlatform agentPlatform;

    public TotController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/tot/ask")
    public TotResult ask(
            @RequestParam(value = "problem", defaultValue = "如何把一个大模型的响应延迟从 5 秒降到 1 秒以内？") String problem) {
        return AgentInvocation.create(agentPlatform, TotResult.class).invoke(new UserInput(problem));
    }
}
