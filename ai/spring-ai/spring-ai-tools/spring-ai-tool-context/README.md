# spring-ai-tool-context — 工具上下文：ToolContext + 动态传参

## 演示内容

工具上下文：ToolContext + 动态传参。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8005/ai/tool-context
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-tool-context spring-boot:run
`
