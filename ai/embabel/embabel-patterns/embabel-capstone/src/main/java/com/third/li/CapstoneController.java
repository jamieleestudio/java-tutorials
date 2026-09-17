package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.AgentProcessStatusCode;
import com.embabel.agent.core.Budget;
import com.embabel.agent.core.Delay;
import com.embabel.agent.core.ProcessControl;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.core.hitl.Awaitable;
import com.embabel.agent.core.hitl.ConfirmationRequest;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 端到端综合示例接口。
 *
 * <p>阶段 5（预算）在这里装配：注意 {@code withBudget} 只设置 budget 字段，
 * 终止策略必须**显式**接进 {@code ProcessControl}（见 {@code embabel-budget} 的坑）。
 */
@RestController
public class CapstoneController {

    /** 单次工单处理的运行预算：最多 6 个动作 / 20 万 token / $1。 */
    private static final Budget BUDGET = new Budget(1.0, 6, 200_000);

    private final AgentPlatform agentPlatform;
    private final TicketMetrics metrics;

    public CapstoneController(AgentPlatform agentPlatform, TicketMetrics metrics) {
        this.agentPlatform = agentPlatform;
        this.metrics = metrics;
    }

    /** 提交工单；高风险会停在 WAITING 等人工确认。 */
    @GetMapping("/capstone/handle")
    public Map<String, Object> handle(
            @RequestParam(value = "message",
                    defaultValue = "客户反馈订单 A1001 商品破损，要求退款，金额 1200") String message) {
        ProcessControl control = new ProcessControl(
                Delay.NONE, Delay.NONE, BUDGET.earlyTerminationPolicy());
        ProcessOptions options = ProcessOptions.DEFAULT.withBudget(BUDGET).withProcessControl(control);

        AgentProcess process = AgentInvocation.builder(agentPlatform)
                .options(options)
                .build(TicketResult.class)
                .run(new UserInput(message));
        return describe(process);
    }

    /** 人工确认（批准 / 拒绝），恢复流程。 */
    @PostMapping("/capstone/{processId}/confirm")
    public Map<String, Object> confirm(
            @PathVariable String processId,
            @RequestParam boolean accepted) {
        AgentProcess process = agentPlatform.getAgentProcess(processId);
        if (process == null) {
            return Map.of("status", "NOT_FOUND", "processId", processId);
        }
        if (!accepted) {
            return Map.of("status", "DECLINED", "processId", processId,
                    "message", "已拒绝，未执行退款");
        }
        Object last = process.lastResult();
        if (last instanceof Awaitable<?, ?> awaitable) {
            process.addObject(awaitable.getPayload());
            process.run();
        }
        return describe(process);
    }

    /** 运行指标（成本 / token / 熔断次数）。 */
    @GetMapping("/capstone/metrics")
    public Map<String, Object> metrics() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("events", metrics.events());
        out.put("terminations", metrics.terminations());
        out.put("tokens", metrics.tokens());
        out.put("costUsd", Math.round(metrics.cost() * 10000) / 10000.0);
        out.put("last", metrics.lastSummary());
        return out;
    }

    private Map<String, Object> describe(AgentProcess process) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("processId", process.getId());
        out.put("status", process.getStatus().name());

        if (process.getStatus() == AgentProcessStatusCode.WAITING
                && process.lastResult() instanceof Awaitable<?, ?> awaitable) {
            if (awaitable instanceof ConfirmationRequest<?> confirmation) {
                out.put("awaitable", "ConfirmationRequest");
                out.put("question", confirmation.getMessage());
                out.put("hint", "POST /capstone/" + process.getId() + "/confirm?accepted=true|false");
            }
        } else if (process.getStatus() == AgentProcessStatusCode.COMPLETED) {
            out.put("output", process.lastResult());
        }
        return out;
    }
}
