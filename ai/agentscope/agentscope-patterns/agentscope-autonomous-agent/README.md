# agentscope-autonomous-agent — Autonomous ReAct agent

## 演示内容

Autonomous ReAct agent。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9205/patterns/autonomous/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-autonomous-agent
- AgentScope 实现：本模块

## 代码结构

- AutonomousAgentAgent.java — HarnessAgent + DeepSeek
- AutonomousAgentAgentController.java — GET /patterns/autonomous/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-autonomous-agent spring-boot:run
`
