# spring-ai-alibaba-mcp-client — MCP 客户端：Streamable HTTP 接入 MCP server

## 演示内容

MCP 客户端：Streamable HTTP 接入 MCP server。

```bash
curl "http://localhost:8517/"
```

> 先启动 8518 服务端再启动本模块。连接配置 streamable-http.connections.*，url 只填 base（端点 /mcp 自动拼接）。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-mcp-client spring-boot:run
```