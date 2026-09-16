# ⑦ 外部集成（embabel-integration）

## 这一章解决什么

把 Agent 接进真实世界：**用别人的能力**（MCP 客户端）、**被别人使用**（MCP server、A2A）、
**换模型来源**（本地 Ollama / 其他厂商）、**多租户与成本**。

## 模块清单

| 模块 | 端口 | 主题 |
|---|---|---|
| [embabel-mcp](../embabel-integration/embabel-mcp/README.md) | 8909 | MCP **客户端**：把外部 MCP server 的工具挂成工具组 |
| [embabel-mcp-server](../embabel-integration/embabel-mcp-server/README.md) | 8927 | MCP **服务端**：把 Agent 暴露给任意 MCP 客户端（SSE） |
| [embabel-a2a](../embabel-integration/embabel-a2a/README.md) | 8910 | A2A 协议：暴露 Agent Card + JSON-RPC，并用 SDK 调用远端 Agent |
| [embabel-ollama](../embabel-integration/embabel-ollama/README.md) | 8925 | 本地模型（完全离线，自动发现模型；需 Docker） |
| [embabel-byok](../embabel-integration/embabel-byok/README.md) | 8926 | 多租户模型路由 + 成本治理（含 BYOK 落点说明） |

## 建议阅读顺序

1. `embabel-mcp` —— 先看"怎么用别人的工具"（客户端视角，最常用）
2. `embabel-mcp-server` —— 再看"怎么把能力给出去"（服务端视角）
3. `embabel-a2a` —— Agent 之间协作的标准协议
4. `embabel-ollama` —— 换模型来源（离线场景）
5. `embabel-byok` —— 多租户与成本（面向 SaaS）

## 与相邻分类的边界

- **MCP 与 ② context 的关系**：MCP 常用来提供"文件/检索"这类上下文工具，
  但它的主题是**协议接入**，故放在本章；内容取舍策略见 ②。
- **A2A 与 ⑧ subagent 的关系**：`embabel-subagent` 是**进程内**委派（把 Agent 当工具）；
  A2A 是**跨进程/跨厂商**的标准协议。
- **模型角色映射**（配置层）在 ④ `embabel-multi-model`；**按租户路由 + 预算**在本章 `embabel-byok`。
