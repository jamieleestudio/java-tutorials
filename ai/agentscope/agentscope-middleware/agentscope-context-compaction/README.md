# agentscope-context-compaction — 上下文压缩

AgentScope 内置 `CompactionMiddleware`，当对话历史超过阈值时自动用模型生成摘要。

## 演示内容

通过 `CompactionConfig` 配置压缩阈值，Agent 自动压缩长对话。

```bash
curl -G --data-urlencode "message=用一句话介绍上下文压缩" http://localhost:9107/compaction/ask
```

> 连续调用多次，观察日志中的 compaction 触发（消息数 > 8 或 token > 2000 时）

## 代码结构

- `ContextCompactionAgent.java` — 配置 CompactionConfig 并通过 `.compaction(config)` 注入
- `ContextCompactionAgentController.java` — 端点

## 关键配置

```java
CompactionConfig config = CompactionConfig.builder()
    .triggerMessages(8)      // 消息数阈值
    .triggerTokens(2000)     // token 数阈值
    .keepMessages(4)         // 保留最近几条
    .keepTokensRatio(0.3)    // 保留比例
    .build();
```

## 运行

```bash
cd ai
mvn -pl :agentscope-context-compaction spring-boot:run
```