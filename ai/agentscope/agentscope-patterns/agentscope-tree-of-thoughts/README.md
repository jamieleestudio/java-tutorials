# agentscope-tree-of-thoughts — Tree of thoughts

## 演示内容

Tree of thoughts。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9216/patterns/tot/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-tree-of-thoughts
- AgentScope 实现：本模块

## 代码结构

- TreeOfThoughtsAgent.java — HarnessAgent + DeepSeek
- TreeOfThoughtsAgentController.java — GET /patterns/tot/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-tree-of-thoughts spring-boot:run
`
