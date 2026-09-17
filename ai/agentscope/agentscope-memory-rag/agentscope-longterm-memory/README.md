# agentscope-longterm-memory — 长期记忆（LongTermMemory + Mem0/ReMe）

## 演示内容

长期记忆（LongTermMemory + Mem0/ReMe）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9125/longterm/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `LongTermMemoryAgent.java` — HarnessAgent + DeepSeek + StaticLongTermMemoryHook
- `Controller` — `GET /longterm/ask`

## 要点

- `LongTermMemory` 接口（record/retrieve），本例用内存实现演示。
- `StaticLongTermMemoryHook` 把记录/检索接入 PreCall/PostCall 钩子。
- 生产环境可换 Mem0/ReMe/向量库实现。

## 运行

```bash
cd ai
mvn -pl :agentscope-longterm-memory spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
