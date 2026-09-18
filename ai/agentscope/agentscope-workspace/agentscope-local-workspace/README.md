# agentscope-local-workspace — 本地工作区（本地文件系统）

## 演示内容

本地工作区（本地文件系统）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9119/workspace/local
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `LocalWorkspaceAgent.java` — HarnessAgent + DeepSeek + LocalFilesystemSpec
- `Controller` — `GET /workspace/local`

## 要点

- `LocalFilesystemSpec` 配置本地工作区：`project`（根目录）、`mode`（SANDBOXED/ROOTED/UNRESTRICTED）。
- `filesystem(spec)` 注入，文件工具限制在工作区内。
- `projectWritable` / `executeTimeoutSeconds` / `maxOutputBytes` 控制权限与资源。

## 运行

```bash
cd ai
mvn -pl :agentscope-local-workspace spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
