package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 并行化接口：两个变体各一个端点。
 */
@RestController
public class ParallelizationController {

    private final AgentPlatform agentPlatform;

    public ParallelizationController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    /** Sectioning：多维度并行评审 + 汇总 */
    @GetMapping("/parallel/sectioning")
    public Report sectioning(
            @RequestParam(value = "message", defaultValue = "用 Redis 缓存热点商品数据并设置 5 分钟过期") String message) {
        return AgentInvocation.create(agentPlatform, Report.class).invoke(new UserInput(message));
    }

    /** Voting：同一内容多视角独立判定 + 投票 */
    @GetMapping("/parallel/voting")
    public Verdict voting(
            @RequestParam(value = "message", defaultValue = "给用户发一封包含折扣码的营销邮件") String message) {
        return AgentInvocation.create(agentPlatform, Verdict.class).invoke(new UserInput(message));
    }
}
