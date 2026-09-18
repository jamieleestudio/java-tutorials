# agentscope-gateway — 网关（HarnessGateway + ChannelRouter）

## 演示内容

网关（HarnessGateway + ChannelRouter）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9129/gateway/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `GatewayAgent.java` — HarnessGateway + ChatUiChannel
- `Controller` — `GET /gateway/ask`

## 要点

- `HarnessGateway.create()` 创建网关，`bindMainAgent` 绑定 Agent。
- `channelManager.register(channel)` 注册渠道，消息统一路由给主 Agent。
- 本例注册 ChatUiChannel，演示多渠道路由架构。

## 运行

```bash
cd ai
mvn -pl :agentscope-gateway spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
