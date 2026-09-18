# agentscope-debate — Multi-agent debate with judge

## 演示内容

Multi-agent debate with judge。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9215/patterns/debate/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-debate
- AgentScope 实现：本模块

## 代码结构

- DebateAgent.java — HarnessAgent + DeepSeek
- DebateAgentController.java — GET /patterns/debate/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-debate spring-boot:run
`
