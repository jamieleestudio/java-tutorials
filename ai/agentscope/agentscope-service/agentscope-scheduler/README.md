# agentscope-scheduler — 定时调度（extensions-scheduler-quartz）

## 演示内容

定时调度（extensions-scheduler-quartz）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9132/scheduler/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `SchedulerAgent.java` — QuartzAgentScheduler + AgentConfig + ScheduleConfig
- `Controller` — `GET /scheduler/ask`

## 要点

- `QuartzAgentScheduler.builder().autoStart(true).build()` 创建调度器。
- `AgentConfig` 定义 Agent（name/sysPrompt/modelConfig），`ScheduleConfig` 定义调度（cron/fixedRate）。
- `scheduler.schedule(agentCfg, scheduleCfg)` 注册定时任务，到点 Quartz 触发。

## 运行

```bash
cd ai
mvn -pl :agentscope-scheduler spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
