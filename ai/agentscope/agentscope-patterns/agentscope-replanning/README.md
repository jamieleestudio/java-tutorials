# agentscope-replanning — Dynamic replanning on tool failure

## 演示内容

Dynamic replanning on tool failure。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9207/patterns/replanning/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-replanning
- AgentScope 实现：本模块

## 代码结构

- ReplanningAgent.java — HarnessAgent + DeepSeek
- ReplanningAgentController.java — GET /patterns/replanning/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-replanning spring-boot:run
`
