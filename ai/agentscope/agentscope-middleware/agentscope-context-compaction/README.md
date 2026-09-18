# agentscope-context-compaction — 上下文压缩（CompactionMiddleware + TokenCounter）

## 演示内容

上下文压缩（CompactionMiddleware + TokenCounter）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9105/compaction/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `ContextCompactionAgent.java` — HarnessAgent + DeepSeek + CompactionConfig
- `Controller` — `GET /compaction/ask`

## 要点

- 通过 `HarnessAgent.Builder.compaction(CompactionConfig)` 配置压缩阈值与保留窗口。
- `CompactionConfig`: `triggerTokens`（触发阈值）、`keepMessages`/`keepTokens`（保留量）、`summaryPrompt`（摘要提示）。
- 对照组 `chatWithoutCompaction` 用 `.disableCompaction()` 关闭压缩，便于对比长对话行为。

## 运行

```bash
cd ai
mvn -pl :agentscope-context-compaction spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
