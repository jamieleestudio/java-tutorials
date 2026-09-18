# agentscope-custom-budget — 自实现预算熔断（用 Middleware 补 Java 缺口）

## 演示内容

自实现预算熔断（用 Middleware 补 Java 缺口）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9106/budget/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `BudgetGuardMiddleware.java` — 自实现预算熔断中间件
- `BudgetAgent.java` — HarnessAgent + DeepSeek + BudgetGuardMiddleware
- `Controller` — `GET /budget/ask`

## 要点

- 自实现 `BudgetGuardMiddleware` 演示 MiddlewareBase 洋葱模型的**短路能力**。
- 累计 `ModelCallEndEvent.getUsage().getTotalTokens()`，预算时不调用 `next`，直接返回熔断结果。
- AgentScope Java 版没有内置预算中间件（Python 版有），这里补上 Java 缺口。

## 运行

```bash
cd ai
mvn -pl :agentscope-custom-budget spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
