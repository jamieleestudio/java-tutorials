package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Playbook 接口。 */
@RestController
public class PlaybookController {

    private final AgentPlatform agentPlatform;

    public PlaybookController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/playbook/release")
    public PlaybookOutcome release(
            @RequestParam(value = "message", defaultValue = "把订单服务的新版本上线到生产环境") String message) {
        return AgentInvocation.create(agentPlatform, PlaybookOutcome.class).invoke(new UserInput(message));
    }
}
