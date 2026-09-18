# agentscope-multi-goal — Multi-goal selection

## 演示内容

Multi-goal selection。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9208/patterns/multi-goal/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-multi-goal
- AgentScope 实现：本模块

## 代码结构

- MultiGoalAgent.java — HarnessAgent + DeepSeek
- MultiGoalAgentController.java — GET /patterns/multi-goal/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-multi-goal spring-boot:run
`
