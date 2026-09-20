# spring-ai-alibaba-agent-scheduling — 定时调度（ScheduledAgentManager + ScheduleConfig）

## 演示内容

定时调度（ScheduledAgentManager + ScheduleConfig）。

```bash
curl "http://localhost:8545/"
```

> fixedRate/cron + 重试 + 生命周期监听。注意 ReactAgent 的图是惰性编译的，调度自建图需先 compile。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-agent-scheduling spring-boot:run
```