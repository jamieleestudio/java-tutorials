# spring-ai-alibaba-mcp-server — MCP 服务端：@Tool 工具 + Streamable HTTP 协议

## 演示内容

MCP 服务端：@Tool 工具 + Streamable HTTP 协议。

```bash
curl "http://localhost:8518/"
```

> yml 需显式 spring.ai.mcp.server.protocol: streamable。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-mcp-server spring-boot:run
```