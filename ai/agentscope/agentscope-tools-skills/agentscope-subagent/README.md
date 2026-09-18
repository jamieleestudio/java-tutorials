# agentscope-subagent — 子代理（SubAgentTool + SubAgentConfig）

## 演示内容

子代理（SubAgentTool + SubAgentConfig）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9113/subagent/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `SubagentAgent.java` — HarnessAgent + DeepSeek + subagentFactory
- `Controller` — `GET /subagent/ask`

## 要点

- `HarnessAgent.Builder.subagentFactory(name, factory)` 注册子 Agent 工厂。
- 主 Agent 把子 Agent 当工具调用，子 Agent 独立运行后返回结果。
- 本例注册"翻译子 Agent"。

## 运行

```bash
cd ai
mvn -pl :agentscope-subagent spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
