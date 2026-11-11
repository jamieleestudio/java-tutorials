# embabel-process-persistence — Agent 进程快照持久化（Embabel 1.5.2+）

## 演示内容

Embabel 1.5.2 新特性：上下文里存在 `AgentProcessSnapshotStore` Bean 即激活进程持久化 —— 平台按 `AgentProcessCheckpointPolicy` 把 AgentProcess（黑板/状态）序列化成快照写入 store，是 HITL 跨重启恢复与进程审计的基础。

- `RecordingSnapshotStore` 包装官方 `InMemoryAgentProcessSnapshotStore`，把每次 save 的元数据暴露成 HTTP
- 两步 Agent（草稿→终稿），观察 checkpoint 处的快照写入
- 生产可无缝换 JCache（cache-core 自动装配）/ JDBC（1.5.3 Blackboard 持久化）

```bash
cd ai/embabel/embabel-ops/embabel-process-persistence
# 需要环境变量 DEEPSEEK_API_KEY（DeepSeek OpenAI 兼容端点）
mvn spring-boot:run
```

> 端口 8943。接口：`POST /persistence/run` → `GET /persistence/snapshots` → `GET /persistence/snapshots/{processId}`。
>
> 与 `embabel-persistence`（ContextRepository 场景）互补：那个演示"上下文对象"持久化，这个演示"进程状态"持久化。
>
> 其余说明见分类 README 与组 README。
