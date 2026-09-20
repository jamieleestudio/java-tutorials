# ⑤ MCP（spring-ai-alibaba-mcp）

## 这一章解决什么

MCP 双端：8518 服务端用 @Tool 注册工具（Streamable HTTP 协议），8517 客户端发现并让模型自主调用远程工具。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [mcp-client](./spring-ai-alibaba-mcp-client/README.md) | 8517 | MCP 客户端 | `GET /mcp/tools` |
| [mcp-server](./spring-ai-alibaba-mcp-server/README.md) | 8518 | MCP 服务端 | `GET /mcp/info` |
