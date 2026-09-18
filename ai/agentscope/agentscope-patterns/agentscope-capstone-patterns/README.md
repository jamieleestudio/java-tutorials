# agentscope-capstone-patterns — End-to-end pattern integration

## 演示内容

End-to-end pattern integration。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9214/patterns/capstone/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-capstone
- AgentScope 实现：本模块

## 代码结构

- CapstoneAgent.java — HarnessAgent + DeepSeek
- CapstoneAgentController.java — GET /patterns/capstone/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-capstone-patterns spring-boot:run
`
