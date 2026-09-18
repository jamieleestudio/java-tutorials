# ② 工具与函数调用（spring-ai-tools）

## 这一章解决什么

让模型调用你的代码：@Tool 函数调用 + ToolContext 动态传参 + MCP 客户端。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [spring-ai-function-calling](./spring-ai-function-calling/README.md) | 8004 | 函数调用 | `GET /ai/function` |
| [spring-ai-tool-context](./spring-ai-tool-context/README.md) | 8005 | 工具上下文 | `GET /ai/tool-context` |
| [spring-ai-mcp-client](./spring-ai-mcp-client/README.md) | 8006 | MCP 客户端 | `GET /ai/mcp` |
