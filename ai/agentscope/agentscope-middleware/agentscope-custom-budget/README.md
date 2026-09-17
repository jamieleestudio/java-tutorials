# agentscope-custom-budget — 自实现预算熔断（用 Middleware 补 Java 缺口）

## 演示内容

自实现预算熔断（用 Middleware 补 Java 缺口）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9106/budget/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `agentscope-custom-budgetAgent.java` — HarnessAgent + DeepSeek
- `Controller` — `GET /budget/ask`

## 要点

本模块是 AgentScope 教程的骨架阶段产物——后续会逐步填充该模块特有的 API 演
（如 Permission/Workspace/Skill 等独有能力）。当前版本确保编译通过、端口不冲突、
结构一致，便于后续迭代。

## 运行

```bash
cd ai
mvn -pl :agentscope-custom-budget spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
