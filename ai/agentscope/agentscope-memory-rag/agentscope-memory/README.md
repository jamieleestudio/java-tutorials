# agentscope-memory — 内存记忆（InMemoryMemory + StateBackedMemory）

## 演示内容

内存记忆（InMemoryMemory + StateBackedMemory）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9124/memory/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `MemoryAgent.java` — HarnessAgent + DeepSeek + MemoryConfig
- `Controller` — `GET /memory/ask`

## 要点

- `MemoryConfig` 配置记忆管理（flush/consolidation/session retention）。
- 默认用 `InMemoryMemory` 存当前对话消息，同会话内记住上下文。
- 不同 sessionId 记忆隔离。

## 运行

```bash
cd ai
mvn -pl :agentscope-memory spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
