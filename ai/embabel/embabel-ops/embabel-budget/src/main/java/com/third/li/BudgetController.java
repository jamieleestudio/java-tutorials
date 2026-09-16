package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.ActionInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.Budget;
import com.embabel.agent.core.Delay;
import com.embabel.agent.core.EarlyTermination;
import com.embabel.agent.core.ProcessControl;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.core.Usage;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行预算与熔断接口。
 *
 * <p>核心 API：
 * <pre>
 *   Budget budget = new Budget(maxCost, maxActions, maxTokens);
 *   ProcessOptions options = ProcessOptions.DEFAULT.withBudget(budget);
 *   AgentInvocation.builder(agentPlatform).options(options).build(Final.class).invoke(input);
 * </pre>
 *
 * <p>{@code Budget} 会展开成一条组合策略
 * （{@code firstOf(maxActions, maxTokens, hardBudgetLimit)}），在**动作之间**检查；
 * 触发时发布 {@link EarlyTermination} 事件（由 {@link BudgetEventListener} 捕获）。
 */
@RestController
public class BudgetController {

    private final AgentPlatform agentPlatform;
    private final BudgetEventListener listener;

    public BudgetController(AgentPlatform agentPlatform, BudgetEventListener listener) {
        this.agentPlatform = agentPlatform;
        this.listener = listener;
    }

    /** 用指定预算跑一次流水线。 */
    @GetMapping("/budget/run")
    public BudgetReport run(
            @RequestParam(value = "message", defaultValue = "为一个小团队设计一套代码评审流程") String message,
            @RequestParam(value = "maxActions", defaultValue = "2") int maxActions,
            @RequestParam(value = "maxTokens", defaultValue = "1000000") int maxTokens,
            @RequestParam(value = "maxCost", defaultValue = "2.0") double maxCost,
            @RequestParam(value = "toolDelay", defaultValue = "NONE") Delay toolDelay) {
        return execute(message, maxCost, maxActions, maxTokens, toolDelay);
    }

    /**
     * 对照实验：同一请求，**紧预算** vs **宽松预算**。
     * 紧预算下流水线被掐断、目标未达成；宽松预算下正常完成。
     */
    @GetMapping("/budget/compare")
    public Map<String, BudgetReport> compare(
            @RequestParam(value = "message", defaultValue = "为一个小团队设计一套代码评审流程") String message) {
        Map<String, BudgetReport> result = new LinkedHashMap<>();
        result.put("tight(maxActions=2)", execute(message, 2.0, 2, 1_000_000, Delay.NONE));
        result.put("generous(默认)", execute(message, 2.0, 50, 1_000_000, Delay.NONE));
        return result;
    }

    private BudgetReport execute(
            String message, double maxCost, int maxActions, int maxTokens, Delay toolDelay) {
        listener.clear();

        Budget budget = new Budget(maxCost, maxActions, maxTokens);

        // ⚠️ 关键坑：ProcessOptions.withBudget(...) **只设置 budget 字段**，
        // 并不会更新 processControl 里的 earlyTerminationPolicy——
        // 那个策略只在 ProcessOptions **构造时**从 budget 派生（见 ProcessOptions 源码）。
        // 所以必须显式把 budget.earlyTerminationPolicy() 接进 ProcessControl，否则预算形同虚设。
        ProcessControl control = new ProcessControl(Delay.NONE, toolDelay, budget.earlyTerminationPolicy());
        ProcessOptions options = ProcessOptions.DEFAULT.withBudget(budget).withProcessControl(control);

        long start = System.nanoTime();
        boolean completed = false;
        String result = null;
        String error = null;
        try {
            Final out = AgentInvocation.builder(agentPlatform)
                    .options(options)
                    .build(Final.class)
                    .invoke(new UserInput(message));
            if (out != null) {
                completed = true;
                result = out.content();
            }
        } catch (Exception e) {
            error = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        EarlyTermination termination = listener.lastTermination();
        AgentProcess process = termination != null ? termination.getAgentProcess() : listener.lastProcess();

        List<String> executedActions = process == null
                ? List.of()
                : process.getHistory().stream().map(ActionInvocation::getActionName).toList();
        int actions = process == null ? 0 : process.getHistory().size();
        double cost = process == null ? 0.0 : process.totalCost();
        Usage usage = process == null ? null : process.totalUsage();

        Map<String, Object> budgetInfo = new LinkedHashMap<>();
        budgetInfo.put("maxCost", maxCost);
        budgetInfo.put("maxActions", maxActions);
        budgetInfo.put("maxTokens", maxTokens);
        budgetInfo.put("toolDelay", toolDelay.name());

        return new BudgetReport(
                message,
                budgetInfo,
                completed,
                executedActions,
                termination != null,
                termination == null ? null : termination.getReason(),
                termination == null ? null : termination.getPolicy().getName(),
                termination == null ? null : termination.getError(),
                actions,
                cost,
                usage == null ? null : usage.getTotalTokens(),
                elapsedMillis,
                result,
                error);
    }
}
