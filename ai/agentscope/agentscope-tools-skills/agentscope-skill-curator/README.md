# agentscope-skill-curator — 技能策展（SkillCurator + SkillPromoter 自动晋升）

## 演示内容

技能策展（SkillCurator + SkillPromoter 自动晋升）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9112/curator/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `CuratorAgent.java` — HarnessAgent + DeepSeek + SkillCurator
- `Controller` — `GET /curator/ask`

## 要点

- `enableSkillCurator(SkillCuratorConfig)` 开启自动策展（晋升/归档）。
- `enableSkillPromotionGate(LocalApprovalGate, EnvironmentFilter)` 设置晋升审批门 + 可见性过滤。
- `agent.runCuratorOnce()` 手动触发一次策展运行。

## 运行

```bash
cd ai
mvn -pl :agentscope-skill-curator spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
