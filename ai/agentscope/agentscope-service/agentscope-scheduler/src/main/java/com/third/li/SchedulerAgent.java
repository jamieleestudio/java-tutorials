package com.third.li;

import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.scheduler.AgentScheduler;
import io.agentscope.extensions.scheduler.ScheduleAgentTask;
import io.agentscope.extensions.scheduler.config.AgentConfig;
import io.agentscope.extensions.scheduler.config.ModelConfig;
import io.agentscope.extensions.scheduler.config.ScheduleConfig;
import io.agentscope.extensions.scheduler.quartz.QuartzAgentScheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时调度 Agent：用 {@link QuartzAgentScheduler}（Quartz 实现）定时触发 Agent 执行任务。
 *
 * <p>调度两层配置：
 * <ul>
 *   <li>{@link AgentConfig} —— Agent 定义（name / sysPrompt / modelConfig）</li>
 *   <li>{@link ScheduleConfig} —— 调度策略（cron / fixedRate / fixedDelay）</li>
 * </ul>
 *
 * <p>{@code scheduler.schedule(agentConfig, scheduleConfig)} 注册一个定时任务，
 * 到点 Quartz 会触发 Agent 运行。
 *
 * <p>本例注册一个每分钟运行的定时 Agent（cron = "0 * * * * ?"）。
 */
@Component
public class SchedulerAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private AgentScheduler scheduler;

    public SchedulerAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return "调度器已启动。定时任务列表：" + scheduledTasks();
    }

    public List<String> scheduledTasks() {
        if (scheduler == null) return List.of();
        return scheduler.getAllScheduleAgentTasks().stream()
                .map(t -> {
                    String cron = "";
                    if (t instanceof io.agentscope.extensions.scheduler.BaseScheduleAgentTask base) {
                        cron = base.getScheduleConfig().getCronExpression();
                    }
                    return t.getName() + " @ " + cron;
                })
                .collect(Collectors.toList());
    }

    @PostConstruct
    void init() {
        scheduler = QuartzAgentScheduler.builder()
                .schedulerId("demo-scheduler")
                .autoStart(true)
                .build();
        ModelConfig modelConfig = new ModelConfig() {
            @Override
            public String getModelName() {
                return modelName;
            }

            @Override
            public OpenAIChatModel createModel() {
                return OpenAIChatModel.builder()
                        .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
            }
        };
        AgentConfig agentCfg = AgentConfig.builder()
                .name("scheduled-agent")
                .sysPrompt("你是一个定时助手，每分钟被触发一次，简短报告当前时间。")
                .modelConfig(modelConfig)
                .build();
        ScheduleConfig scheduleCfg = ScheduleConfig.builder()
                .cron("0 * * * * ?")
                .build();
        scheduler.schedule(agentCfg, scheduleCfg);
    }

    @PreDestroy
    void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}