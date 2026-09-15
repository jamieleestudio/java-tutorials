# embabel-a2a — A2A（Agent2Agent）服务端与客户端

## 演示内容

本应用同时扮演两个角色：

- **A2A 服务端**：引入 `embabel-agent-starter-a2a` 后，平台上的目标会自动变成 A2A 的 **skill**，
  并暴露 Agent Card 与 JSON-RPC 端点
- **A2A 客户端**：拉取远端 Agent Card → 用 SDK 建客户端 → `message/send` 发消息

另外演示 **Agent-to-Agent 协作**：本地 Agent 把"远端 A2A 智能体"当作**工具**来用。

## 关键 API / 端点

| 项 | 说明 |
|---|---|
| `embabel-agent-starter-a2a` | 服务端自动装配（Agent Card + JSON-RPC） |
| `GET /a2a/.well-known/agent.json` | Agent Card（技能、传输方式、协议版本） |
| `POST /a2a` | A2A JSON-RPC 端点（`message/send` 等） |
| `io.a2a.A2A` / `io.a2a.client.Client` | SDK 客户端：`getAgentCard` / `Client.builder(card)` / `sendMessage` |
| `io.a2a.spec.*` | 协议类型：`AgentCard`、`Message`、`TextPart`、`DataPart`、`Task` |

## 接口

```bash
# 以客户端身份拉取远端 Agent Card
curl http://localhost:8910/a2a/card

# 直接调用远端 A2A 智能体
curl -G --data-urlencode "message=用一句话介绍 Embabel" http://localhost:8910/a2a/ask

# 本地 Agent 把远端智能体当工具调用（Agent-to-Agent）
curl -G --data-urlencode "message=请远端智能体用一句话说明什么是 A2A 协议" http://localhost:8910/a2a/delegate
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-a2a spring-boot:run
```

默认 `demo.remote-a2a-url` 指向**本应用自己**（单进程自包含演示）。
要演示跨进程协作，启动第二个实例（`--server.port=8912`）并把
`REMOTE_A2A_URL` 指向它即可。

## 代码结构

- `SupportAgent.java` — 被暴露为 A2A skill 的 Agent
- `A2AClient.java` — SDK 客户端：拉卡片 → 建客户端 → 发消息 → 提取文本
- `RemoteA2ATools.java` — 把远端智能体包装成 `@LlmTool`
- `LocalAgent.java` — 用该工具完成委派（输出类型与 SupportAgent 区分开，避免目标冲突）
- `A2aController.java` — card / ask / delegate 三个端点

## 要点（踩坑记录）

- **Agent Card 路径不一致**：SDK 0.3.2 的 `A2A.getAgentCard(url)` 默认取
  `/.well-known/agent-card.json`，而 Embabel 1.0.0 注册的是 `/.well-known/agent.json`（旧路径），
  直接用它拿卡片会 404。本示例改用 `RestClient` 自己取卡片，再交给 SDK 建客户端。
- **A2A 端点前缀是 `/a2a`**：启动日志会打印
  `Registering web endpoint under /a2a/.well-known/agent.json`。
- **强类型产出在 `DataPart` 里**：Embabel 把 Agent 的产出（如 `ChatReply`）作为结构化数据放在
  `DataPart`（JSON，形如 `{output={content=...}}`），不是 `TextPart`；提取回复时要解包。
- **客户端要关流式**：卡片声明 `streaming=true` 时，SDK 默认走流式并给出 `TaskUpdateEvent`；
  用 `ClientConfig.builder().setStreaming(false)` 可拿到完整结果，简化演示。
- 两个 Agent 若产出同一个类型会造成"目标冲突"，所以本地委派 Agent 用 `DelegatedReply` 区分。
