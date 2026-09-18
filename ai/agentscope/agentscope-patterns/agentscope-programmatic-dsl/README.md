# agentscope-programmatic-dsl — Programmatic builder DSL

## 演示内容

Programmatic builder DSL。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9218/patterns/dsl/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-programmatic-dsl
- AgentScope 实现：本模块

## 代码结构

- ProgrammaticDslAgent.java — HarnessAgent + DeepSeek
- ProgrammaticDslAgentController.java — GET /patterns/dsl/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-programmatic-dsl spring-boot:run
`
