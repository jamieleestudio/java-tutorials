package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.Usage;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 可观测性接口：运行一次 Agent，返回步骤耗时、成本、token、模型与捕获到的事件。
 *
 * <p>进程自身的统计来自 {@link AgentProcess}：
 * {@code getHistory()}（动作历史）、{@code totalCost()}、{@code totalUsage()}、{@code ownModelsUsed()}。
 * 事件明细来自注入的 {@link MetricsEventListener}（同时也打印在应用日志里）。
 */
@RestController
public class ObservabilityController {

    private final AgentPlatform agentPlatform;
    private final MetricsEventListener listener;

    public ObservabilityController(AgentPlatform agentPlatform, MetricsEventListener listener) {
        this.agentPlatform = agentPlatform;
        this.listener = listener;
    }

    @GetMapping("/observability/run")
    public RunReport run(
            @RequestParam(value = "message", defaultValue = "为什么要给 Agent 做可观测性？") String message) {
        AgentProcess process = AgentInvocation.create(agentPlatform, Answer.class)
                .run(new UserInput(message));

        List<String> steps = process.getHistory().stream()
                .map(step -> "%s (%dms)".formatted(step.getActionName(), step.getRunningTime().toMillis()))
                .toList();

        List<String> models = process.ownModelsUsed().stream()
                .map(LlmMetadata::getName)
                .distinct()
                .toList();

        Usage usage = process.totalUsage();

        return new RunReport(
                process.getStatus().name(),
                steps,
                process.totalCost(),
                usage.getTotalTokens(),
                models,
                listener.eventsFor(process.getId()),
                listener.llmCalls(),
                listener.actionRuns());
    }
}
