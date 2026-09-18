# agentscope-orchestrator-workers — Orchestrator-workers with dynamic subagent tasks

## 演示内容

Orchestrator-workers with dynamic subagent tasks。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9203/patterns/orchestrator/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-orchestrator-workers
- AgentScope 实现：本模块

## 代码结构

- OrchestratorWorkersAgent.java — HarnessAgent + DeepSeek
- OrchestratorWorkersAgentController.java — GET /patterns/orchestrator/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-orchestrator-workers spring-boot:run
`
