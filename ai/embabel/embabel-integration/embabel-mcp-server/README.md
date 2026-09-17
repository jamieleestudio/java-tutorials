# embabel-mcp-server — 把 Agent 暴露为 MCP Server

## 演示内容

与 `embabel-mcp`（客户端：我们用别人的工具）**对称**——这里把 Embabel Agent 的能力
通过 **MCP（Model Context Protocol）** 暴露出去，任何 MCP 客户端（MCP Inspector、Claude Desktop 等）
都能连接并调用。

## 关键 API / 端点

| 项 | 说明 |
|---|---|
| `GET /sse` | 建立 SSE 事件流，返回 message endpoint（含 sessionId） |
| `POST /mcp/message?sessionId=...` | 发送 JSON-RPC 请求（initialize / tools/list / tools/call） |
| `HttpServletSseServerTransportProvider` | MCP SDK 的 SSE 传输（注册成 Servlet） |
| `McpServer.sync(transport).serverInfo(...).tool(tool, handler).build()` | 组装 MCP server |
| `McpSchema.Tool` / `CallToolResult` | 工具定义与返回结构 |

## 接口验证（手工 JSON-RPC）

```bash
# 1) 建立 SSE 流（会返回 data: /mcp/message?sessionId=xxx）
curl -N http://localhost:8927/sse

# 2) 另开一个终端，用上面的 sessionId 发请求
curl -X POST -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"probe","version":"1.0"}}}' \
  "http://localhost:8927/mcp/message?sessionId=xxx"

curl -X POST -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"notifications/initialized"}' \
  "http://localhost:8927/mcp/message?sessionId=xxx"

curl -X POST -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}' \
  "http://localhost:8927/mcp/message?sessionId=xxx"
# SSE 流里会收到 tools/list 响应，包含 ask_embabel
```

实测（SSE 流中捕获）：

```json
{"jsonrpc":"2.0","id":1,"result":{"protocolVersion":"2024-11-05","capabilities":{"tools":{}},
  "serverInfo":{"name":"embabel-mcp-server","version":"1.0.0"},"instructions":"..."}}
{"jsonrpc":"2.0","id":2,"result":{"tools":[{"name":"ask_embabel",
  "description":"向 Embabel Agent 提问并返回回答...",
  "inputSchema":{"type":"object","properties":{"question":{"type":"string"}}}}]}}
```

调用工具时，会转成一次 Embabel Agent 调用（`AgentInvocation.create(platform, Reply.class)`）。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-mcp-server spring-boot:run
# 然后用 MCP Inspector 连接 http://localhost:8927/sse
```

## 代码结构

- `McpServerConfig.java` — 传输（SSE Servlet）+ server（serverInfo/tools）+ 工具实现（转 Agent 调用）
- `QaAgent.java` — 被暴露的 Agent
- `McpServerApplication.java` — 入口

## 要点（踩坑记录）

- **官方 `embabel-agent-starter-mcp-server` 在当前依赖仓库里不可用**（m2 里只有 `.lastUpdated`），
  因此本模块用 MCP SDK 的 **server 端类**（`mcp-core`，随 `spring-ai-mcp` 传递进来）手工装配。
  这反而更清楚：MCP server = **传输 + 服务声明 + 工具实现** 三部分。
- **SSE 是双向的**：POST 只返回 202，真正的响应通过 SSE 流回传——所以验证时要先开流、再发请求。
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
> `embabel-agent-starter-mcp-server` 在 1.0.0 的 m2 里只有 `.lastUpdated`（不可用）。
