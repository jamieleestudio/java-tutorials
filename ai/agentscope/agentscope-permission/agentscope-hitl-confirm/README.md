# agentscope-hitl-confirm — 人工确认（RequireUserConfirmEvent）

## 演示内容

人工确认（RequireUserConfirmEvent）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9117/hitl/confirm
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `HitlConfirmAgent.java` — HarnessAgent + DeepSeek + ASK 规则 + ConfirmLoggingHook
- `Controller` — `GET /hitl/confirm`

## 要点

- ASK 权限规则触发 `RequireUserConfirmEvent`，用户用 `UserConfirmResultEvent` 回复。
- `ConfirmResult` 携带确认/拒绝 + 规则建议。
- `ConfirmLoggingHook` 演示接入点；生产可接 Webhook / IM 审批。

## 运行

```bash
cd ai
mvn -pl :agentscope-hitl-confirm spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
