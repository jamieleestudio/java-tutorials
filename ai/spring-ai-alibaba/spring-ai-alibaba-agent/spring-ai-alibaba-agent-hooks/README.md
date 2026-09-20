# spring-ai-alibaba-agent-hooks — Hook 体系（SummarizationHook + PIIDetectionHook + ToolCallLimitHook）

## 演示内容

Hook 体系（SummarizationHook + PIIDetectionHook + ToolCallLimitHook）。

```bash
curl "http://localhost:8543/"
```

> 摘要 Hook 在 token 超阈值时压缩旧消息；PII Hook 用正则检测并打码。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-agent-hooks spring-boot:run
```