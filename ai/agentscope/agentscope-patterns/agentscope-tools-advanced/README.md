# agentscope-tools-advanced — Progressive tools and self-reflection

## 演示内容

Progressive tools and self-reflection。

`ash
curl -G --data-urlencode "message=你好" http://localhost:9211/patterns/tools-advanced/ask
`

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用可能返回 500。
> 代码结构已就绪，充值后即可验证。

## 与 Embabel 的对照

- Embabel 实现：embabel-tools-advanced
- AgentScope 实现：本模块

## 代码结构

- ToolsAdvancedAgent.java — HarnessAgent + DeepSeek
- ToolsAdvancedAgentController.java — GET /patterns/tools-advanced/ask

## 运行

`ash
cd ai
mvn -pl :agentscope-tools-advanced spring-boot:run
`
