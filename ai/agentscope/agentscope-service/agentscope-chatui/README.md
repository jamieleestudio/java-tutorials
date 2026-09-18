# agentscope-chatui — ChatUI 渠道（浏览器可点）

## 演示内容

ChatUI 渠道（浏览器可点）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9130/chatui/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `ChatUiAgent.java` — HarnessGateway + ChatUiChannel
- `Controller` — `GET /chatui/ask`

## 要点

- `ChatUiChannel.create(config)` 创建内置 Web UI 渠道。
- `channel.send(message)` 发消息给 Agent，`pollOutbound()` 供前端拉取。
- 注册到 HarnessGateway，绑定主 Agent 处理消息。

## 运行

```bash
cd ai
mvn -pl :agentscope-chatui spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
