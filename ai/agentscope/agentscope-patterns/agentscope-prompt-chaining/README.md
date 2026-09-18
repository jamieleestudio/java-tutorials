# agentscope-prompt-chaining — Prompt chaining with gate middleware

## 演示内容

Prompt chaining with gate middleware。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9200/patterns/prompt-chain/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-prompt-chaining
- AgentScope 实现：本模块

## 代码结构

- PromptChainingAgent.java — HarnessAgent + DeepSeek
- PromptChainingAgentController.java — GET /patterns/prompt-chain/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-prompt-chaining spring-boot:run
`
