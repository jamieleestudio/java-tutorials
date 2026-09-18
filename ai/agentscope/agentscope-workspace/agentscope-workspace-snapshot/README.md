# agentscope-workspace-snapshot — 工作区快照/恢复

## 演示内容

工作区快照/恢复

```bash
curl -G --data-urlencode "message=你好" http://localhost:9122/workspace/snapshot
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `SnapshotAgent.java` — HarnessAgent + DeepSeek + DockerFilesystemSpec + LocalSnapshotSpec
- `Controller` — `GET /workspace/snapshot`

## 要点

- `LocalSnapshotSpec(path)` 实现快照持久化到本地目录。
- `DockerFilesystemSpec.snapshotSpec(spec)` 注入快照策略。
- `SandboxSnapshot.persist/restore` 保存与恢复沙箱状态。

## 运行

```bash
cd ai
mvn -pl :agentscope-workspace-snapshot spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
