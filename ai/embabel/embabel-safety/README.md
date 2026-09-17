# ⑤ 质量与安全（embabel-safety）

## 这一章解决什么

Agent 能"做事"就意味着能"做错事"。这一章讲三层防护：**内容层面的护栏**（提示词注入、敏感信息）、
**能力层面的最小权限**（只暴露该暴露的工具）、**数据层面的隔离**（谁只能看到哪些数据）。

## 模块清单

| 模块 | 端口 | 主题 |
|---|---|---|
| [embabel-guardrails](../embabel-safety/embabel-guardrails/README.md) | 8898 | 输入/输出护栏（`UserInputGuardRail` / `AssistantMessageGuardRail`，CRITICAL 阻断） |
| [embabel-secure-tools](../embabel-safety/embabel-secure-tools/README.md) | 8924 | 工具安全（最小权限只读工具 + PII 输入/输出检测） |
| [embabel-identity](../embabel-safety/embabel-identity/README.md) | 8939 | 身份与租户隔离（`Identities`/`User` + `ToolCallContext` 带外透传到工具） |

## 建议阅读顺序

1. `embabel-guardrails` —— 先掌握护栏机制（校验、严重级别、阻断）
2. `embabel-secure-tools` —— 再看工程实践（最小权限 + PII + 敏感操作确认）
3. `embabel-identity` —— 最后解决"**谁只能看到哪些数据**"（身份 + 租户隔离必须显式实现）

## 与相邻分类的边界

- **"模型能否调用某工具"是安全边界**：不给就不会被调用（见 `embabel-secure-tools`），
  比事后过滤可靠；**工具的实现安全**（沙箱）见 ② `embabel-file-tools`。
- **人工审批**属于交互手段（③ `embabel-hitl` / `embabel-hitl-advanced`），常与本章的"敏感操作"配合使用。
- **模型访问的租户隔离**见 ⑦ `embabel-byok`（每租户 Key 与成本）；
  **数据访问的租户隔离**见本章 `embabel-identity`。两者互补，别混为一谈。
- **成本失控**（另一种"安全"）见 ⑥ `embabel-budget`（超限即终止）。
- **评估**（③→⑥）用于验证质量，见 ⑥ `embabel-eval`。
