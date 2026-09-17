# agentscope-model-providers — 多模型 Provider

## 演示内容

三种模型使用方式：直接构建、按需切换、自动 fallback。

```bash
# 当前配置
curl http://localhost:9104/model/info

# 用主模型（Flash，快且便宜）
curl -G --data-urlencode "message=用一句话解释什么是 MoE 架构" http://localhost:9104/model/primary

# 手动切到 Pro 模型（更强但更贵）
curl -G --data-urlencode "message=用一句话解释什么是 MoE 架构" http://localhost:9104/model/pro
```

> ⚠️ 实测时 DeepSeek 余额不足（HTTP 402 `Insufficient Balance`）导致 500——
> 这恰好证明了 **fallback 机制**的价值：如果配了 `fallbackModel`，主模型 402 时会自动切到备用模型。
> 充值后两个端点都会正常返回。

## 关键 API

| API | 作用 |
|---|---|
| `OpenAIChatModel.builder().apiKey().modelName().baseUrl().build()` | 直接构建模型实例 |
| `HarnessAgent.builder().model(Model)` | 注入主模型 |
| `HarnessAgent.builder().fallbackModel(Model)` | 注入备用模型（主模型失败时自动切） |
| `HarnessAgent.builder().maxRetries(n)` | 重试次数（失败后先重试 n 次再切 fallback） |
| `ModelRegistry.register(name, Model)` / `resolve(name)` | 按名注册/解析模型（配置驱动） |

## 与 Embabel 的对照

| | Embabel | AgentScope |
|---|---|---|
| 多模型 | `ai.withLlmByRole("fast")` 按**角色**选模型 | `fallbackModel()` 按**失败**切模型 |
| 角色映射 | yml `embabel.models.llms.fast: deepseek-flash` | `ModelRegistry` 按名注册 |
| 降级 | 无内置（需自己 try-catch） | `fallbackModel` + `maxRetries` 内置自动降级 |

**区别**：Embabel 的多模型是"不同任务用不同模型"（角色映射）；
AgentScope 的 fallback 是"同一个任务，主模型挂了就切备用"（容错降级）。两者互补。

## 代码结构

- `MultiModelAgent.java` — 主模型 + fallback + 按需切换
- `ModelProvidersController.java` — `/model/primary`、`/model/pro`、`/model/info`

## 要点

- **fallback 是调用级的**：不是"启动时选一个模型"，而是"每次调用时，主模型失败 → 重试 → 还失败 → 切 fallback"。这比 Embabel 的角色映射更适合"模型偶尔抽风"的场景。
- **`ModelRegistry` 支持按名解析**：`HarnessAgent.builder().model("deepseek-v4-flash")` 也能用（通过 `ModelRegistry.resolve`），适合配置驱动。本模块用直接构建是为了展示两个层次。
- AgentScope Java 支持的 Provider 扩展（Maven Central 上都有 starter）：
  `agentscope-openai-spring-boot-starter` / `agentscope-dashscope-spring-boot-starter` /
  `agentscope-anthropic-spring-boot-starter` / `agentscope-gemini-spring-boot-starter` /
  `agentscope-ollama-spring-boot-starter`——换 Provider 只需换依赖 + 配 baseUrl。