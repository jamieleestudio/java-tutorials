# spring-ai-alibaba-graph-tool-hitl — 工具级 HITL（工具执行前挂起 + 人工审批）

## 演示内容

工具级 HITL（工具执行前挂起 + 人工审批）。

```bash
curl "http://localhost:8547/"
```

> interruptBefore + InterruptableAction（InterruptionMetadata/ToolFeedback 元数据）+ updateState 写审批结果 + resume。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-graph-tool-hitl spring-boot:run
```