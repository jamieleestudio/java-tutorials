# embabel-mcp-server — 把 Agent 暴露为 MCP Server（Streamable HTTP）

## 演示内容

与 `embabel-mcp`（客户端：我们用别人的工具）**对称**——这里把 Embabel Agent 的能力
通过 **MCP（Model Context Protocol）** 暴露出去，任何 MCP 客户端（MCP Inspector、Claude Desktop 等）
都能连接并调用。

> **1.5.3 迁移说明**：Embabel 官方已把 MCP server 从**已废弃的 SSE 传输**迁移到
> **Streamable HTTP**。旧协议需要 `/sse` + `/mcp/message` 两个端点配合；
> 新协议只有一个 `/mcp` 端点：POST（JSON-RPC 请求/响应）、GET（可选 SSE 推送流）、DELETE（结束会话）。

## 关键 API / 端点

| 项 | 说明 |
|---|---|
| `POST /mcp` | 发送 JSON-RPC 请求（initialize / tools/list / tools/call），响应可为普通 JSON 或 SSE 流 |
| `GET /mcp` | 可选：建立服务器推送流（SSE 帧） |
| `DELETE /mcp` | 结束会话 |
| `HttpServletStreamableServerTransportProvider` | MCP SDK 的 Streamable HTTP 传输（注册成 Servlet） |
| `McpServer.sync(transport).serverInfo(...).tool(tool, handler).build()` | 组装 MCP server |
| `McpSchema.Tool` / `CallToolResult` | 工具定义与返回结构 |

## 接口验证（手工 JSON-RPC）

```bash
# 1) initialize（Streamable HTTP：一个 POST 完成，响应里带 Mcp-Session-Id 头）
curl -i -X POST -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"probe","version":"1.0"}}}' \
  "http://localhost:8927/mcp"

# 2) 用返回的 Mcp-Session-Id 继续请求（header 或 URL 均可，以下用 header）
curl -X POST -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <上一步返回的 sessionId>" \
  -d '{"jsonrpc":"2.0","method":"notifications/initialized"}' \
  "http://localhost:8927/mcp"

curl -X POST -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <sessionId>" \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}' \
  "http://localhost:8927/mcp"
# 响应包含 ask_embabel 工具定义

curl -X POST -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <sessionId>" \
  -d '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"ask_embabel","arguments":{"question":"什么是 GOAP 规划"}}}' \
  "http://localhost:8927/mcp"

# 3) 用完结束会话
curl -X DELETE -H "Mcp-Session-Id: <sessionId>" "http://localhost:8927/mcp"
```

调用工具时，会转成一次 Embabel Agent 调用（`AgentInvocation.create(platform, Reply.class)`）。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-mcp-server spring-boot:run
# 然后用 MCP Inspector 连接 http://localhost:8927/mcp（Streamable HTTP）
```

## 代码结构

- `McpServerConfig.java` — 传输（Streamable HTTP Servlet）+ server（serverInfo/tools）+ 工具实现（转 Agent 调用）
- `QaAgent.java` — 被暴露的 Agent
- `McpServerApplication.java` — 入口

## 要点（踩坑记录）

- **官方 `embabel-agent-starter-mcp-server` 在当前依赖仓库里不可用**（m2 里只有 `.lastUpdated`），
  因此本模块用 MCP SDK 的 **server 端类**（`mcp-core`，随 `spring-ai-mcp` 传递进来）手工装配。
  这反而更清楚：MCP server = **传输 + 服务声明 + 工具实现** 三部分。
- **SSE → Streamable HTTP 的核心变化**：旧协议 POST 只回 202、响应走独立 SSE 流（先开流再发请求）；
  新协议 POST 直接返回结果（无状态请求），需要服务器推送时才开 GET 流，会话由 `Mcp-Session-Id` 维系。
- 工具的参数 schema 要自己写 JSON Schema（这里用 `McpSchema.JsonSchema`）；
  对比 Embabel 的 `@LlmTool`（自动生成 schema）。
- 生产上还应加：鉴权（`ServerTransportSecurityValidator`）、工具白名单、
  以及把危险工具排除在外（见 `embabel-secure-tools`）。


## 附：MCP 的 resources / prompts（本模块只暴露了 tools）

MCP 协议除 `tools` 外还有两类能力，本模块**只实现了 tools**：

| 能力 | 用途 | 在 `McpServer` 上的注册方式 |
|---|---|---|
| `tools` | 模型可调用的函数 | `.tool(...)`（本模块已实现） |
| `resources` | 只读的"资源"（文件内容、数据库行等），由客户端拉取或订阅 | `.resources(...)` |
| `prompts` | 服务端预置的提示词模板，供客户端选用 | `.prompts(...)` |

什么时候需要它们：如果你的"工具"其实是**只读数据源**（比如把内部文档暴露给 IDE），
用 `resources` 比 `tools` 更贴协议语义（客户端可以直接列出/订阅，而不是让模型去调函数）。

> 本模块用 `mcp-core` 的 server 端类手工装配，因为
> `embabel-agent-starter-mcp-server` 在当前 m2 里只有 `.lastUpdated`（不可用）。
