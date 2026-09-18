# agentscope-playbook — Conditional skill playbook

## 演示内容

Conditional skill playbook。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9213/patterns/playbook/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-playbook
- AgentScope 实现：本模块

## 代码结构

- PlaybookAgent.java — HarnessAgent + DeepSeek
- PlaybookAgentController.java — GET /patterns/playbook/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-playbook spring-boot:run
`
