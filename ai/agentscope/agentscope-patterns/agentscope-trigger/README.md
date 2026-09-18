# agentscope-trigger — Reactive trigger via Hook or MessageBus

## 演示内容

Reactive trigger via Hook or MessageBus。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9209/patterns/trigger/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-trigger
- AgentScope 实现：本模块

## 代码结构

- TriggerAgent.java — HarnessAgent + DeepSeek
- TriggerAgentController.java — GET /patterns/trigger/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-trigger spring-boot:run
`
