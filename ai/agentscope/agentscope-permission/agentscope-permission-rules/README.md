# agentscope-permission-rules — 权限规则（PermissionRule + PermissionEngine）

## 演示内容

权限规则（PermissionRule + PermissionEngine）

```bash
curl -G --data-urlencode "message=你好" http://localhost:9116/permission/rules
```

> ⚠️ 当前 DeepSeek 余额不足（HTTP 402），LLM 调用会返回 500。
> 代码结构已就绪，充值后即可正常返回。

## 代码结构

- `PermissionRuleAgent.java` — HarnessAgent + DeepSeek + PermissionRule
- `Controller` — `GET /permission/rules`

## 要点

- `PermissionRule(toolName, ruleContent, behavior, source)` 定义规则。
- 三种行为：ALLOW（放行）/ DENY（拒绝）/ ASK（确认）。
- 规则装入 `PermissionContextState`，`PermissionEngine` 运行时自动匹配决策。

## 运行

```bash
cd ai
mvn -pl :agentscope-permission-rules spring-boot:run
```

> 需要 `OPENAI_API_KEY` 环境变量（DeepSeek key）。
