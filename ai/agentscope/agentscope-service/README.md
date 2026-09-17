# ⑦ 服务与渠道 ★（agentscope-service）

## 这一章解决什么

AgentScope 的服务化——网关、渠道、调度器、A2A 协议——把 Agent 从"库调用"变成"服务"。

## 模块清单

| 模块 | 端口 | 主题 | 接口 |
|---|---|---|---|
| [agentscope-gateway](agentscope-gateway/README.md) | 9129 | 网关（HarnessGateway + GatewayBootstrap） | `GET /gateway/routed` |
| [agentscope-chatui](agentscope-chatui/README.md) | 9130 | ChatUI 渠道（ChatUiChannel send/poll） | `GET /chatui/send` |
| [agentscope-channels](agentscope-channels/README.md) | 9131 | 多渠道路由（ChannelManager + Channel） | `GET /channels/ask` |
| [agentscope-scheduler](agentscope-scheduler/README.md) | 9132 | 调度器（WakeupDispatcher + MessageBus） | `GET /scheduler/ask` |
| [agentscope-a2a](agentscope-a2a/README.md) | 9133 | A2A 协议（RemoteTarget + RemoteSubagentStub） | `GET /a2a/ask` |

## 架构

```
消息入站 → Channel → Gateway → Agent → 回复 → Channel → 出站
              ↑
        ChannelManager（管理多渠道）
              ↑
        WakeupDispatcher（事件驱动唤醒）
```

## Gateway 核心

```java
HarnessGateway gateway = HarnessGateway.create(channelManager);
gateway.bindMainAgent(agent);
gateway.run(msgContext, msgs);  // 同步调用
gateway.runStream(msgContext, msgs);  // 流式调用
```