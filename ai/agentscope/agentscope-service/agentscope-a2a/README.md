# agentscope-a2a — A2A 协议（extensions-a2a）

## 演示内容

A2A 协议（extensions-a2a）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9133/a2a/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `A2aAgent.java` — AgentScopeA2aServer + HarnessAgentRunner
- `Controller` — `GET /a2a/ask`

## 要点

- `AgentScopeA2aServer.builder(agentRunner).agentCard(card).build()` 创建 A2A 服务。
- `AgentRunner` 适配本地 HarnessAgent 为 A2A 可调用形式。
- 发布 AgentCard（name/description/skills），支持 JSON-RPC over HTTP/SSE 远程调用。

## 运行

```bash
cd ai
mvn -pl :agentscope-a2a spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
