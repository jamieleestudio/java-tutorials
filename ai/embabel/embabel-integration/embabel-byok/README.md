# embabel-byok — 多租户模型路由与成本治理

## 演示内容

- **多租户模型路由**：租户等级 `basic` → 便宜模型、`pro` → 强模型（配置驱动，不写死在代码里）
- **成本治理**：累计每租户的调用次数/token/成本，超过上限后**拒绝服务**
- **BYOK 的落点**：真正的"每个用户自带 Key"见下方说明与代码片段

## 关键 API

| API | 作用 |
|---|---|
| `embabel.models.llms.{role: model}` | 角色 → 模型映射（本模块 basic / pro） |
| `ai.withLlmByRole("pro")` | 按角色选择模型 |
| `ai.withLlm("模型名")` / `ai.withAutoLlm()` | 按名字 / 自动选择 |
| `AgentProcess.totalUsage()` / `totalCost()` | 跑完后读取用量与成本（治理依据） |
| `Budget` / `EarlyTerminationPolicy` | 框架内置的预算与早停（需经 `ProcessOptions` 传入） |

## 接口

```bash
# 按租户等级路由
curl -G --data-urlencode "tenant=acme" --data-urlencode "tier=pro" --data-urlencode "message=..." \
     http://localhost:8926/byok/ask

# 查看租户用量
curl -G --data-urlencode "tenant=acme" http://localhost:8926/byok/usage
```

实测（`--demo.tenant-cost-limit=0.00001` 时）：

```json
// 第 1 次：正常回答，标记已超限
{"answer":"你好！有什么可以帮你的吗？","cost":9.75E-5,"tokens":118,"overBudget":true}
// 第 2 次：被拒绝
{"rejected":true,"reason":"租户已超出成本上限，请充值或联系管理员","usage":{...}}
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-byok spring-boot:run
# 演示拒绝：--demo.tenant-cost-limit=0.00001
```

## 代码结构

- `TenantAgent.java` — `ai.withLlmByRole(tier)` 路由
- `TenantLedger.java` — 租户用量台账 + 成本上限
- `ByokController.java` — `/byok/ask`、`/byok/usage`

## 要点

- **真正的 BYOK（每用户自带 Key）**：框架提供 `ByokFactory` / `ProviderDetection`
  用于识别"这个 Key 属于哪个 Provider"，再用 `OpenAiCompatibleModelFactory`
  按该 Key 构造 `LlmService`，最后 `ai.withLlmService(llmService)` 走这次调用：

  ```java
  var factory = new OpenAiCompatibleModelFactory(baseUrl, userApiKey, "/chat/completions",
          null, Map.of(), ObservationRegistry.NOOP, restClientProvider, webClientProvider);
  LlmService<?> llm = factory.buildValidated("user-model", pricingModel, modelId, null);
  ai.withLlmService(llm).generateText(prompt);
  ```

  本模块用"租户 → 模型角色 + 成本台账"落地可运行版本，避免在示例里管理密钥生命周期。
- **框架的 `Budget`** 有 cost / actions / tokens 三个上限并能生成早停策略，
  但需要通过 `ProcessOptions` 传入（Java 侧构造参数较多），
  因此示例采用"跑完后读 `totalUsage()/totalCost()`"的应用级治理。
- 多租户还涉及**身份**（`User` / `Identities`）与**会话隔离**（`contextId`），
  见 `embabel-conversation` 与 `embabel-persistence`。
