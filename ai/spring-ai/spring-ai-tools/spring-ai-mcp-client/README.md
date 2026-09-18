# spring-ai-mcp-client — MCP 客户端：接入外部 MCP server

## 演示内容

MCP 客户端：接入外部 MCP server。

`ash
curl -G --data-urlencode "message=你好" http://localhost:8006/ai/mcp
`

> 需要 OPENAI_API_KEY 环境变量（DeepSeek key）。

## 运行

`ash
cd ai/spring-ai
mvn -pl :spring-ai-mcp-client spring-boot:run
`
