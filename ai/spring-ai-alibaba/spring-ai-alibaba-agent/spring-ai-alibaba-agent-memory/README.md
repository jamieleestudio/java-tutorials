# spring-ai-alibaba-agent-memory — Agent 会话记忆：saver(MemorySaver) + threadId

## 演示内容

Agent 会话记忆：saver(MemorySaver) + threadId。

```bash
curl "http://localhost:8512/"
```

> 同一 threadId 多轮共享历史，跨 threadId 隔离。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-agent-memory spring-boot:run
```