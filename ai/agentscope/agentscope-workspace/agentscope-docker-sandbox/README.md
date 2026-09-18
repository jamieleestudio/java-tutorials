# agentscope-docker-sandbox — Docker 沙箱（隔离执行）

## 演示内容

Docker 沙箱（隔离执行）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9120/workspace/docker
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `DockerSandboxAgent.java` — HarnessAgent + DeepSeek + DockerFilesystemSpec
- `Controller` — `GET /workspace/docker`

## 要点

- `DockerFilesystemSpec`（继承 SandboxFilesystemSpec）配置 Docker 沙箱。
- `image` / `workspaceRoot` / `memorySizeBytes` / `cpuCount` / `network` 控制容器。
- `network("none")` 断网隔离。运行需宿主机安装 Docker。

## 运行

```bash
cd ai
mvn -pl :agentscope-docker-sandbox spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
