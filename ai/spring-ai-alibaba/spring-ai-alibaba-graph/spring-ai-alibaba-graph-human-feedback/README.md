# spring-ai-alibaba-graph-human-feedback — 人机协同：interruptBefore 挂起 + updateState 注入反馈 + resume 恢复

## 演示内容

人机协同：interruptBefore 挂起 + updateState 注入反馈 + resume 恢复。

```bash
curl "http://localhost:8508/"
```

> 恢复前必须用 compiledGraph.updateState 写入反馈（addStateUpdate 不落状态）。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-graph-human-feedback spring-boot:run
```