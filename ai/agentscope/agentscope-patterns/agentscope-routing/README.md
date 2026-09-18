# agentscope-routing — Routing pattern via classification

## 演示内容

Routing pattern via classification。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9201/patterns/routing/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-routing
- AgentScope 实现：本模块

## 代码结构

- RoutingAgent.java — HarnessAgent + DeepSeek
- RoutingAgentController.java — GET /patterns/routing/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-routing spring-boot:run
`
