package com.third.li;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.scheduling.DefaultScheduledAgentManager;
import com.alibaba.cloud.ai.graph.scheduling.ScheduleConfig;
import com.alibaba.cloud.ai.graph.scheduling.ScheduledAgentManager;
import com.alibaba.cloud.ai.graph.scheduling.ScheduledAgentTask;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import jakarta.annotation.PreDestroy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 定时调度：ScheduledAgentManager + ScheduleConfig。
 *
 * <p>SAA 可以把 Agent 图当作可调度的"任务单元"：
 * <ul>
 *   <li>{@link ScheduleConfig} — cron / fixedRate / fixedDelay + 固定输入 + 重试策略</li>
 *   <li>{@code compiledGraph.schedule(config)} — 把编译后的图注册成 {@link ScheduledAgentTask}</li>
 *   <li>{@link DefaultScheduledAgentManager} — 全局调度管理器：注册/注销/查询任务</li>
 * </ul>
 *
 * <p>注意：ReactAgent 的 compiledGraph 是惰性构建的（首次 call 才初始化），
 * 要调度 Agent 图需要先触发编译，或像本模块一样自建 StateGraph。
 *
 * <p>典型用途：定时巡检、周期性报告、轮询数据源后触发告警 —— 给 Agent 装上"闹钟"。
 */
@Service
public class AgentSchedulingService {

    private final ScheduledAgentManager manager = DefaultScheduledAgentManager.getInstance();
    private final List<String> runLog = new CopyOnWriteArrayList<>();
    private final String taskId;

    public AgentSchedulingService(ChatModel chatModel) throws GraphStateException {
        StateGraph graph = new StateGraph("scheduled_tips", () -> Map.of(
                        "input", new ReplaceStrategy(),
                        "output", new ReplaceStrategy()))
                .addNode("tips", node_async(state -> {
                    String tip = chatModel.call("每次只输出一句不超过 40 字的 Java/AI 技术小贴士，不要重复。");
                    return Map.of("output", tip);
                }))
                .addEdge(StateGraph.START, "tips")
                .addEdge("tips", StateGraph.END);

        CompiledGraph compiled = graph.compile();

        // 每 15 秒执行一次；生命周期监听器记录每次执行的输出
        ScheduleConfig config = ScheduleConfig.builder()
                .fixedRate(15_000)
                .initialDelay(5_000)
                .inputs(Map.of("input", "生成今日技术小贴士"))
                .runnableConfig(RunnableConfig.builder()
                        .threadId("scheduled-" + UUID.randomUUID()).build())
                .maxRetries(2)
                .retryDelay(java.time.Duration.ofSeconds(3))
                .addListener(new com.alibaba.cloud.ai.graph.scheduling.ScheduleLifecycleListener() {
                    @Override
                    public void onEvent(com.alibaba.cloud.ai.graph.scheduling.ScheduleLifecycleListener.ScheduleEvent event,
                                        Object payload) {
                        runLog.add(LocalDateTime.now() + " → [" + event + "] " + payload);
                    }
                })
                .build();

        ScheduledAgentTask task = compiled.schedule(config);
        this.taskId = manager.registerTask(task);
    }

    /** 查询调度任务状态与执行记录。 */
    public String status() {
        var optionalTask = manager.getTask(taskId);
        return "taskId: " + taskId
                + "\n活跃任务数: " + manager.getActiveTaskCount()
                + "\n任务存在: " + optionalTask.isPresent()
                + "\n执行记录:\n" + String.join("\n", runLog);
    }

    /** 注销任务。 */
    public String cancel() {
        boolean removed = manager.unregisterTask(taskId);
        return removed ? "任务已注销：" + taskId : "任务不存在或已注销：" + taskId;
    }

    @PreDestroy
    void shutdown() {
        manager.shutdown();
    }
}
