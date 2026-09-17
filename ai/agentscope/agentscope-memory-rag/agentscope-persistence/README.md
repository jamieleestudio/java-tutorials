# agentscope-persistence — 持久化（state + session-redis/mysql）

## 演示内容

持久化（state + session-redis/mysql）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9128/persistence/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `PersistenceAgent.java` — HarnessAgent + DeepSeek + JsonFileAgentStateStore
- `Controller` — `GET /persistence/ask`

## 要点

- `stateStore(JsonFileAgentStateStore)` 把 AgentState 持久化到磁盘 JSON 文件。
- 重启后同 sessionId/userId 能恢复会话上下文。
- 对照组 `disableSessionPersistence()` 关闭持久化，重启即丢。

## 运行

```bash
cd ai
mvn -pl :agentscope-persistence spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
