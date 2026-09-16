package com.third.li;

import com.embabel.agent.api.common.StuckHandlerResult;
import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.ActionInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Function;

/**
 * 卡住处理接口。
 *
 * <ul>
 *   <li>{@code /stuck/recover} —— 有 StuckHandler：卡住 → 补前提 → 重规划 → 完成</li>
 *   <li>{@code /stuck/no-handler} —— 对照组：没有 StuckHandler，进程停在 STUCK</li>
 * </ul>
 */
@RestController
public class StuckController {

    private final AgentPlatform agentPlatform;
    private final StuckEventListener listener;

    public StuckController(AgentPlatform agentPlatform, StuckEventListener listener) {
        this.agentPlatform = agentPlatform;
        this.listener = listener;
    }

    /** 有 StuckHandler 兜底：应当能自动恢复并完成。 */
    @GetMapping("/stuck/recover")
    public StuckOutcome recover(
            @RequestParam(value = "message", defaultValue = "评估一下把内部知识库迁到 pgvector 是否值得") String message) {
        return run(
                message,
                input -> AgentInvocation.create(agentPlatform, Report.class)
                        .invoke(input)
                        .content());
    }

    /** 对照组：没有 StuckHandler，进程停在 STUCK，目标无法达成。 */
    @GetMapping("/stuck/no-handler")
    public StuckOutcome noHandler(
            @RequestParam(value = "message", defaultValue = "评估一下把内部知识库迁到 pgvector 是否值得") String message) {
        return run(
                message,
                input -> AgentInvocation.create(agentPlatform, UnhandledReport.class)
                        .invoke(input)
                        .content());
    }

    private StuckOutcome run(String message, Function<UserInput, String> invoke) {
        listener.clear();

        boolean achieved = false;
        String result = null;
        String error = null;
        try {
            result = invoke.apply(new UserInput(message));
            achieved = result != null;
        } catch (Exception e) {
            error = e.getClass().getSimpleName() + ": " + e.getMessage();
        }

        AgentProcess process = listener.lastProcess();
        List<StuckHandlerResult> handlerResults = listener.handlerResults();

        String status = process == null ? "UNKNOWN" : process.getStatus().name();
        List<String> actions = process == null
                ? List.of()
                : process.getHistory().stream().map(ActionInvocation::getActionName).toList();

        return new StuckOutcome(
                message,
                achieved,
                result,
                status,
                process != null && "STUCK".equals(status),
                handlerResults.size(),
                handlerResults.stream().map(r -> r.getCode().name()).toList(),
                handlerResults.stream().map(StuckHandlerResult::getMessage).toList(),
                actions,
                error);
    }
}
