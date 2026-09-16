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
 * 多租户接口：按租户等级路由模型，并累计/限制每租户成本。
 *
 * <p>试试：
 * <pre>
 *   ?tenant=acme&tier=pro&message=...
 *   ?tenant=acme&tier=basic&message=...
 *   GET /byok/usage?tenant=acme
 * </pre>
 */
@RestController
public class ByokController {

    private final AgentPlatform agentPlatform;
    private final TenantLedger ledger;

    public ByokController(AgentPlatform agentPlatform, TenantLedger ledger) {
        this.agentPlatform = agentPlatform;
        this.ledger = ledger;
    }

    @GetMapping("/byok/ask")
    public Map<String, Object> ask(
            @RequestParam(value = "tenant", defaultValue = "acme") String tenant,
            @RequestParam(value = "tier", defaultValue = "basic") String tier,
            @RequestParam(value = "message", defaultValue = "用一句话说明多租户模型路由的价值") String message) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tenant", tenant);
        result.put("tier", tier);

        if (ledger.overBudget(tenant)) {
            result.put("rejected", true);
            result.put("reason", "租户已超出成本上限，请充值或联系管理员");
            result.put("usage", ledger.snapshot(tenant));
            return result;
        }

        AgentProcess process = AgentInvocation.create(agentPlatform, TenantAnswer.class)
                .run(new TenantRequest(tenant, tier), new UserInput(message));

        TenantAnswer answer = process.last(TenantAnswer.class);
        double cost = process.totalCost();
        Integer usedTokens = process.totalUsage().getTotalTokens();
        boolean overBudget = ledger.record(tenant, usedTokens, cost);

        result.put("answer", answer == null ? null : answer.content());
        result.put("cost", cost);
        result.put("tokens", usedTokens);
        result.put("overBudget", overBudget);
        result.put("usage", ledger.snapshot(tenant));
        return result;
    }

    @GetMapping("/byok/usage")
    public Map<String, Object> usage(@RequestParam(value = "tenant", defaultValue = "acme") String tenant) {
        return ledger.snapshot(tenant);
    }
}
