# embabel-multi-model — 多模型：角色映射与回退

## 演示内容

- **按角色选模型**：`fast` 角色用便宜模型列要点，`deep` 角色用强模型写终稿
- **模型回退**：按候选顺序挑第一个可用模型（未注册的自动跳过）

## 关键 API

| API | 作用 |
|---|---|
| `embabel.models.llms.{role: model-name}` | 角色 → 模型名映射（模型名须已注册） |
| `ai.withLlmByRole("fast")` | 按角色取 PromptRunner |
| `ai.withLlm("deepseek-v4-pro")` | 按模型名取 |
| `ai.withAutoLlm()` | 自动选择（可结合 prompt/工具） |
| `ai.withFirstAvailableLlmOf("a", "b")` | 按顺序回退到第一个可用模型 |

## 配置

```yaml
embabel:
  models:
    default-llm: deepseek-flash
    llms:
      fast: deepseek-flash
      deep: deepseek-v4-pro
```

## 接口

```bash
# 角色路由：fast 列要点 -> deep 写终稿
curl -G --data-urlencode "message=如何为一个客服场景设计 Agent？" http://localhost:8905/multi-model/ask

# 模型回退：gpt-5.4 未注册 -> 自动用 deepseek-flash
curl -G --data-urlencode "message=一句话说明什么是模型回退" http://localhost:8905/multi-model/fallback
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-multi-model spring-boot:run
```

## 代码结构

- `MultiModelAgent.java` — `withLlmByRole("fast")` → `withLlmByRole("deep")`
- `FallbackAgent.java` — `withFirstAvailableLlmOf("gpt-5.4", "deepseek-flash")`
- `MultiModelController.java` — 两个端点

## 要点

- 角色映射在启动时校验：`llms` 里写了未注册的模型名会直接报错（"LLM 'x' for role y is not available"）。
- 回退时日志会打印 `Requested LLM 'xxx' not found`，可据此观察实际选择。
- 这套机制很适合**成本优化**（粗活给便宜模型）与**多租户**（不同租户映射不同模型）。
- 还有 `embabel.agent.platform.ranking.llm` 可单独指定"目标/Agent 排序"所用的模型。
