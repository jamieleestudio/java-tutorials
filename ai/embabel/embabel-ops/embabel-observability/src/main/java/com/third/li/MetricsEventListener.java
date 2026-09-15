package com.third.li;

import com.embabel.agent.api.event.ActionExecutionResultEvent;
import com.embabel.agent.api.event.ActionExecutionStartEvent;
import com.embabel.agent.api.event.AgentProcessCompletedEvent;
import com.embabel.agent.api.event.AgentProcessEvent;
import com.embabel.agent.api.event.AgentProcessFailedEvent;
import com.embabel.agent.api.event.AgentProcessStuckEvent;
import com.embabel.agent.api.event.AgenticEventListener;
import com.embabel.agent.api.event.LlmResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事件监听器：把 Agent 执行过程中的事件记录下来，并打印到日志。
 *
 * <p>注册方式很简单——只要声明成 Spring Bean（这里是 {@code @Component}），
 * Embabel 的 {@code AgentPlatformConfiguration} 会把容器里所有 {@link AgenticEventListener}
 * 组合成一个多播监听器，自动接收平台与进程事件。
 *
 * <p>常用事件：
 * <ul>
 *   <li>{@link ActionExecutionStartEvent} / {@link ActionExecutionResultEvent}：动作开始/结束（含耗时与状态）</li>
 *   <li>{@link LlmResponseEvent}：每次 LLM 调用结束（含模型名与耗时）</li>
 *   <li>{@code AgentProcessCompletedEvent} / {@code AgentProcessFailedEvent} / {@code AgentProcessStuckEvent}：终态</li>
 * </ul>
 */
@Component
public class MetricsEventListener implements AgenticEventListener {

    private static final Logger log = LoggerFactory.getLogger(MetricsEventListener.class);

    private final Map<String, List<String>> eventsByProcess = new ConcurrentHashMap<>();
    private final AtomicInteger llmCalls = new AtomicInteger();
    private final AtomicInteger actionRuns = new AtomicInteger();

    @Override
    public void onProcessEvent(AgentProcessEvent event) {
        List<String> events = eventsByProcess.computeIfAbsent(
                event.getProcessId(),
                id -> Collections.synchronizedList(new ArrayList<>()));

        if (event instanceof ActionExecutionStartEvent start) {
            actionRuns.incrementAndGet();
            events.add("action:start " + start.getAction().getName());
            log.info("[observability] action start: {}", start.getAction().getName());
        } else if (event instanceof ActionExecutionResultEvent result) {
            events.add("action:done %s status=%s %dms".formatted(
                    result.getAction().getName(),
                    result.getActionStatus().getStatus(),
                    result.getRunningTime().toMillis()));
        } else if (event instanceof LlmResponseEvent<?> response) {
            llmCalls.incrementAndGet();
            String model = response.getRequest().getLlmMetadata().getName();
            long millis = response.getRunningTime().toMillis();
            events.add("llm:response model=%s %dms".formatted(model, millis));
            log.info("[observability] llm response: model={} {}ms", model, millis);
        } else if (event instanceof AgentProcessCompletedEvent) {
            events.add("process:completed");
            log.info("[observability] process completed: {}", event.getProcessId());
        } else if (event instanceof AgentProcessFailedEvent) {
            events.add("process:failed");
        } else if (event instanceof AgentProcessStuckEvent) {
            events.add("process:stuck");
        }
    }

    public List<String> eventsFor(String processId) {
        List<String> events = eventsByProcess.get(processId);
        return events == null ? List.of() : List.copyOf(events);
    }

    public int llmCalls() {
        return llmCalls.get();
    }

    public int actionRuns() {
        return actionRuns.get();
    }
}
