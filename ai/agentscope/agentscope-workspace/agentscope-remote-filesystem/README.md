# agentscope-remote-filesystem — 远程文件系统抽象

## 演示内容

远程文件系统抽象

```bash
curl -G --data-urlencode "message=你好" http://localhost:9121/workspace/remote
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `RemoteFsAgent.java` — HarnessAgent + DeepSeek + RemoteFilesystemSpec
- `Controller` — `GET /workspace/remote`

## 要点

- `RemoteFilesystemSpec(BaseStore)` 把文件操作路由到远程存储后端。
- 本例用 `InMemoryStore` 演示；生产可换 Redis/S3/DB backed store。
- `addSharedPrefix` 声明跨会话共享路径前缀。

## 运行

```bash
cd ai
mvn -pl :agentscope-remote-filesystem spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
