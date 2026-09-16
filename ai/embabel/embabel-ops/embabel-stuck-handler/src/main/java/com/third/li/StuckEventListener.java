package com.third.li;

import com.embabel.agent.api.common.StuckHandlerResult;
import com.embabel.agent.api.event.AbstractAgentProcessEvent;
import com.embabel.agent.api.event.AgentProcessEvent;
import com.embabel.agent.api.event.AgenticEventListener;
import com.embabel.agent.core.AgentProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 观察卡住与补救过程。
 *
 * <p>两个关键点：
 * <ul>
 *   <li>{@link StuckHandlerResult} 本身就是一个 {@code AgentProcessEvent}，
 *       框架在调用完 handler 后会把它发布出来——所以能拿到"补救结果 + 用了哪个 handler + 结论码"。</li>
 *   <li>同时记录最近一次看到的 {@link AgentProcess}，这样**没有** handler 的对照组
 *       也能读到进程状态（STUCK）。</li>
 * </ul>
 */
@Component
public class StuckEventListener implements AgenticEventListener {

    private static final Logger log = LoggerFactory.getLogger(StuckEventListener.class);

    private final AtomicReference<AgentProcess> lastProcess = new AtomicReference<>();
    private final List<StuckHandlerResult> handlerResults = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onProcessEvent(AgentProcessEvent event) {
        if (event instanceof AbstractAgentProcessEvent processEvent) {
            lastProcess.set(processEvent.getAgentProcess());
        }
        if (event instanceof StuckHandlerResult result) {
            handlerResults.add(result);
            log.info("StuckHandler 结果：code={}, message={}", result.getCode(), result.getMessage());
        }
    }

    public AgentProcess lastProcess() {
        return lastProcess.get();
    }

    public List<StuckHandlerResult> handlerResults() {
        return List.copyOf(handlerResults);
    }

    public void clear() {
        lastProcess.set(null);
        handlerResults.clear();
    }
}
