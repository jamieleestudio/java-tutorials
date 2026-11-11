# langchain4j-pattern-confidence-cascade — Confidence Cascade（置信度级联）

## 演示内容

LangChain4j 1.23 新增 agentic 模式：便宜模型先草答 → 决策智能体（`decisionAgentBuilder` + `@Decide` + `threshold`）评估置信度 → 置信度不足时 conditional 升级到贵模型重答。

- `deepseek-chat` 草稿（便宜）
- 决策模型打分，阈值 0.6，结果写入 `state.confident`
- 不达标才触发 `deepseek-reasoner` 深度重答（贵）

```bash
cd ai/langchain4j/langchain4j-patterns/langchain4j-pattern-confidence-cascade
# 需要环境变量 DEEPSEEK_API_KEY（DeepSeek）
mvn spring-boot:run
```

> 端口 8727。接口：`GET /patterns/cascade?request=…`（用简单问题走便宜通道，用复杂问题触发升级）。
>
> 其余说明见分类 README 与组 README。
