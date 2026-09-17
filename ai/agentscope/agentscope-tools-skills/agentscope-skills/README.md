# agentscope-skills — 技能系统（SkillBox + SkillRegistry + DynamicSkillMiddleware）

## 演示内容

技能系统（SkillBox + SkillRegistry + DynamicSkillMiddleware）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9111/skills/ask
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `SkillAgent.java` — HarnessAgent + DeepSeek + SkillBox
- `Controller` — `GET /skills/ask`

## 要点

- `SkillBox` 内部持有 `SkillRegistry`，`registration().skill(...).tool(...).apply()` 注册技能。
- 技能 = 提示词 + 工具 + 资源，比单工具更高层，可激活/停用。
- 本例注册"天气查询"技能并激活。

## 运行

```bash
cd ai
mvn -pl :agentscope-skills spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
