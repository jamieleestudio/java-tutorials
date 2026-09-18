# agentscope-permission-modes — 权限模式（bypass/confirm/strict）

## 演示内容

权限模式（bypass/confirm/strict）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9115/permission/modes
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `PermissionModeAgent.java` — HarnessAgent + DeepSeek + PermissionContextState
- `Controller` — `GET /permission/modes`

## 要点

- `permissionContext(PermissionContextState)` 注入权限模式（BYPASS/DONT_ASK/DEFAULT）。
- `agent.setPermissionMode(ctx, mode)` 运行时动态切换。
- `stopOnReject(true)` 表示权限拒绝时立即终止 Agent。

## 运行

```bash
cd ai
mvn -pl :agentscope-permission-modes spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
