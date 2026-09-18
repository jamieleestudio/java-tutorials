# agentscope-subagent-handoff — Subagent handoff delegation

## 演示内容

Subagent handoff delegation。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9210/patterns/subagent/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-subagent
- AgentScope 实现：本模块

## 代码结构

- SubagentAgent.java — HarnessAgent + DeepSeek
- SubagentAgentController.java — GET /patterns/subagent/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-subagent-handoff spring-boot:run
`
