# spring-ai-alibaba-graph-checkpoint — 状态 checkpoint：MemorySaver + threadId + getStateHistory

## 演示内容

状态 checkpoint：MemorySaver + threadId + getStateHistory。

```bash
curl "http://localhost:8507/"
```

> 同一 threadId 多轮对话共享状态（AppendStrategy 追加）。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-graph-checkpoint spring-boot:run
```