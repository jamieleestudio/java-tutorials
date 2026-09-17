# agentscope-custom-budget — 自实现预算熔断

填补 AgentScope Java 的空白：Python 版有 `BudgetControlMiddleware`，Java 版没有。

## 演示内容

通过 `MiddlewareBase.onReasoning` 钩子自实现 token / 迭代次数双限，超限短路。

```bash
# 带预算（maxTokens=500, maxIters=3）——长对话会被短路
curl -G --data-urlencode "message=请详细介绍 Spring Boot" http://localhost:9106/budget/ask

# 不带预算（对照组）
curl -G --data-urlencode "message=请详细介绍 Spring Boot" http://localhost:9106/budget/unlimited

# 预算报告
curl http://localhost:9106/budget/report
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `BudgetControlMiddleware.java` — 自定义中间件，在 `onReasoning` 中检查预算
- `BudgetAgent.java` — 带预算和不带预算的 Agent 对比
- `BudgetAgentController.java` — 3 个端点：ask / unlimited / report

## 关键设计

1. **预算检查在推理之前**——`onReasoning` 钩子在调模型前执行
2. **短路**——不调 `next.apply(input)` 时模型不会被调用，Flux 直接 complete
3. **双限**——token 累计估算 + 迭代次数，任一超限即短路
4. **比 Embabel 更精确**——Embabel 在 action 之间检查（会超调一轮），这里在下一轮推理前阻止

## 运行

```bash
cd ai
mvn -pl :agentscope-custom-budget spring-boot:run
```