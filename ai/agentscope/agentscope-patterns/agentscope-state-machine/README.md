# agentscope-state-machine — State machine with plan mode

## 演示内容

State machine with plan mode。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9217/patterns/state-machine/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-state-machine
- AgentScope 实现：本模块

## 代码结构

- StateMachineAgent.java — HarnessAgent + DeepSeek
- StateMachineAgentController.java — GET /patterns/state-machine/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-state-machine spring-boot:run
`
