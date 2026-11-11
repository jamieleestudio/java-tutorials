# embabel-decision-service — 类型安全决策/分类服务（Embabel 1.5.4）

## 演示内容

Embabel 1.5.4 新特性：`Ai.decisions()` / `Ai.classifications()` ServiceSelector + 平台自动注册的 `LlmDecisionServiceFactory`（LLM 后端；亦可换 System One 协议决策模型，调用代码不变）。

- `classify(text, ClassificationSpec)`：多分类，返回 `Selected`（categoryId + confidence + 概率分布）
- `ask(text, DecisionSpec)`：一次问一组问题 —— proposition（是/否 + pTrue）、choice（单选）、rating（评级 + 选中级别）

```bash
cd ai/embabel/embabel-reasoning/embabel-decision-service
# 需要环境变量 DEEPSEEK_API_KEY（DeepSeek OpenAI 兼容端点）
mvn spring-boot:run
```

> 端口 8941。接口：`GET /reasoning/decisions?message=…` 返回 TicketReport（分类/退款意图/优先级/紧急度/建议）。
>
> 其余说明见分类 README 与组 README。
