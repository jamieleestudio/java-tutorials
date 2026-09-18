# agentscope-interrupt — 实时打断（Agent.interrupt + InterruptControl）

## 演示内容

实时打断（Agent.interrupt + InterruptControl）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9118/interrupt/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `InterruptAgent.java` — HarnessAgent + DeepSeek + interrupt
- `Controller` — `GET /interrupt/ask`

## 要点

- `Agent.interrupt()` / `interrupt(Msg)` 运行时中止 ReAct 循环。
- 用流式调用 `stream(...)` 演示：收到首个事件后立即 interrupt。
- `InterruptControl` / `InterruptSource` 记录打断来源与待执行工具。

## 运行

```bash
cd ai
mvn -pl :agentscope-interrupt spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
