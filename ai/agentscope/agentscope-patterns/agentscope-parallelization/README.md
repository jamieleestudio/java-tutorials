# agentscope-parallelization — Parallelization and voting with multiple agents

## 演示内容

Parallelization and voting with multiple agents。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9202/patterns/parallel/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-parallelization
- AgentScope 实现：本模块

## 代码结构

- ParallelizationAgent.java — HarnessAgent + DeepSeek
- ParallelizationAgentController.java — GET /patterns/parallel/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-parallelization spring-boot:run
`
