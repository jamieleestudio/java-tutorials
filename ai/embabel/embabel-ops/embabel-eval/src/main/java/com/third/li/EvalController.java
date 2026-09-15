package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评估接口。
 *
 * <p>注意：一次评估会跑 {@code 用例数 × 2} 次 LLM 调用（作答 + 评审），耗时较长。
 */
@RestController
public class EvalController {

    private final AgentPlatform agentPlatform;

    public EvalController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/eval/run")
    public EvalReport run() {
        return AgentInvocation.create(agentPlatform, EvalReport.class).invoke(new UserInput("run-eval"));
    }
}
