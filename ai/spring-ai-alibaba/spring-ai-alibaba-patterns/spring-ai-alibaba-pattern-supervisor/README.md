# spring-ai-alibaba-pattern-supervisor — 主管编排：supervisor 决策循环分发 worker（图循环）

## 演示内容

主管编排：supervisor 决策循环分发 worker（图循环）。

```bash
curl "http://localhost:8522/"
```

> recursionLimit 兜底防死循环；messages 用 AppendStrategy 留轨迹。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-pattern-supervisor spring-boot:run
```