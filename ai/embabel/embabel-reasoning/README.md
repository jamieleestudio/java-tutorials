# ④ 推理与规划（embabel-reasoning）

## 这一章解决什么

**模型怎么想、用哪个模型想**：把推理过程取出来、换规划器策略、按角色/回退选模型。

## 模块清单

| 模块 | 端口 | 主题 |
|---|---|---|
| [embabel-thinking](../embabel-reasoning/embabel-thinking/README.md) | 8896 | 推理过程提取（`Thinking.withExtraction()` + `<think>` 标签） |
| [embabel-planner-types](../embabel-reasoning/embabel-planner-types/README.md) | 8900 | 规划器对比（`PlannerType.GOAP` vs `UTILITY`，含 `@Cost` 动态价值） |
| [embabel-multi-model](../embabel-reasoning/embabel-multi-model/README.md) | 8905 | 角色→模型映射与回退（`withLlmByRole` / `withFirstAvailableLlmOf`） |

## 建议阅读顺序

1. `embabel-planner-types` —— 先理解"谁决定动作顺序"（GOAP 要目标、UTILITY 按价值）
2. `embabel-multi-model` —— 再理解"哪个模型干活"（角色路由 + 回退）
3. `embabel-thinking` —— 最后看"模型怎么想"（推理与答案分离）

## 与相邻分类的边界

- **规划器机制**（GOAP/UTILITY 对比）在本章；**编排模式**（主管 / 路由 / 并行 / 自主循环）
  在 ⑧ `embabel-patterns`——前者是"引擎参数"，后者是"结构模式"。
- **模型角色映射**在本章；**按租户/用户路由 + 成本治理** 在 ⑦ `embabel-byok`。
- **早停与预算**（`EarlyTerminationPolicy` / `Budget`）待补 `embabel-early-termination`。
