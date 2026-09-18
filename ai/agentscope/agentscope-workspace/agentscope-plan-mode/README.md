# agentscope-plan-mode — 计划模式（enterPlanMode/exitPlanMode）

## 演示内容

计划模式（enterPlanMode/exitPlanMode）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9123/workspace/plan
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `PlanModeAgent.java` — HarnessAgent + DeepSeek + enablePlanMode
- `Controller` — `GET /workspace/plan`

## 要点

- `enablePlanMode()` 开启计划模式，注入 `PlanModeMiddleware`。
- `enterPlanMode(ctx)` / `exitPlanMode(ctx)` 运行时控制。
- 计划阶段只读，用户确认后才执行；`planFileDirectory` 指定 plan 文件目录。

## 运行

```bash
cd ai
mvn -pl :agentscope-plan-mode spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
