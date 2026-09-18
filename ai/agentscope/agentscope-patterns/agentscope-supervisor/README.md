# agentscope-supervisor — Supervisor managing subagents

## 演示内容

Supervisor managing subagents。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9206/patterns/supervisor/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-supervisor
- AgentScope 实现：本模块

## 代码结构

- SupervisorAgent.java — HarnessAgent + DeepSeek
- SupervisorAgentController.java — GET /patterns/supervisor/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-supervisor spring-boot:run
`
