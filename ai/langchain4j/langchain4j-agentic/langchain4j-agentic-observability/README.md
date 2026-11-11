# langchain4j-agentic-observability — AgentMonitor + HTML 执行报告 + AI Service 汇总事件

## 演示内容

AgentMonitor + HTML 执行报告；1.23 新增 `AiServiceInvocationSummaryEvent`（一次 AI Service 调用的全部事件汇总，经 `AiServices.registerListeners` 注册监听）。

```bash
cd ai/langchain4j/langchain4j-agentic/langchain4j-agentic-observability
# 需要环境变量 DEEPSEEK_API_KEY（DeepSeek）
mvn spring-boot:run
```

> 端口 8721。接口：`GET /ai/agentic/monitor`、`GET /ai/agentic/monitor/report`、`GET /ai/agentic/summary`（1.23 汇总事件）。
>
> 其余说明见分类 README 与组 README。
