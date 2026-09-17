# agentscope-background-tasks — 后台任务（BackgroundTask + TaskRepository）

## 演示内容

后台任务（BackgroundTask + TaskRepository）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9114/background/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `BackgroundTaskAgent.java` — HarnessAgent + DeepSeek + enableTaskList
- `Controller` — `GET /background/ask`

## 要点

- `enableTaskList()` 注册内置 TaskTool，Agent 可创建/查询/取消后台任务。
- `asyncToolTimeout(Duration)` 设置异步工具超时，超时则转入后台。
- 底层由 `TaskRepository` 持久化任务记录到工作区。

## 运行

```bash
cd ai
mvn -pl :agentscope-background-tasks spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
