# agentscope-channels — IM 渠道（钉钉/飞书/企微）

## 演示内容

IM 渠道（钉钉/飞书/企微）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9131/channels/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `ChannelAgent.java` — HarnessGateway + DingTalk/Feishu/WeCom
- `Controller` — `GET /channels/ask`

## 要点

- `DingTalkChannel.fromProperties` / `FeishuChannel.fromProperties` / `WeComChannel.fromProperties` 创建 IM 渠道。
- 凭据从配置读，未配置时跳过该渠道。
- 三渠道注册到同一网关，消息统一路由给主 Agent。

## 运行

```bash
cd ai
mvn -pl :agentscope-channels spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
