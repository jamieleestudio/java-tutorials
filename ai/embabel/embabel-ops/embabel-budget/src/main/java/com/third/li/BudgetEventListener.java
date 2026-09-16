package com.third.li;

import com.embabel.agent.api.event.AbstractAgentProcessEvent;
import com.embabel.agent.api.event.AgentProcessEvent;
import com.embabel.agent.api.event.AgenticEventListener;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.EarlyTermination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 观察**提前终止**事件。
 *
 * <p>预算/策略触发熔断时，框架会发布一个 {@link EarlyTermination} 事件
 * （它是 {@code AgentProcessEvent} 的子类）。注册一个 {@code AgenticEventListener} Bean
 * 就能拿到它——这是"知道进程为什么被掐断"的官方途径。
 *
 * <p>同时记录最近一次看到的 {@link AgentProcess}，这样即使**没有**被熔断，
 * 也能读到动作数 / 累计成本 / token 用量。
 *
 * <p>注意：为了示例简洁，这里是"最近一次"的全局状态，不区分并发进程；
 * 生产环境应按 {@code processId} 分桶（事件里有 {@code getProcessId()}）。
 */
@Component
public class BudgetEventListener implements AgenticEventListener {

    private static final Logger log = LoggerFactory.getLogger(BudgetEventListener.class);

    private final AtomicReference<EarlyTermination> lastTermination = new AtomicReference<>();
    private final AtomicReference<AgentProcess> lastProcess = new AtomicReference<>();

    @Override
    public void onProcessEvent(AgentProcessEvent event) {
        // getAgentProcess() 定义在 AbstractAgentProcessEvent 上（不在 AgentProcessEvent 接口上）
        if (event instanceof AbstractAgentProcessEvent processEvent) {
            lastProcess.set(processEvent.getAgentProcess());
        }
        if (event instanceof EarlyTermination termination) {
            lastTermination.set(termination);
            log.warn("提前终止：policy={}, error={}, reason={}",
                    termination.getPolicy().getName(), termination.getError(), termination.getReason());
        }
    }

    public EarlyTermination lastTermination() {
        return lastTermination.get();
    }

    public AgentProcess lastProcess() {
        return lastProcess.get();
    }

    public void clear() {
        lastTermination.set(null);
        lastProcess.set(null);
    }
}
