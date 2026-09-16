# ⑤ 质量与安全（embabel-safety）

## 这一章解决什么

Agent 能"做事"就意味着能"做错事"。这一章讲两类防护：**内容层面的护栏**（提示词注入、敏感信息）
与**能力层面的最小权限**（只暴露该暴露的工具）。

## 模块清单

| 模块 | 端口 | 主题 |
|---|---|---|
| [embabel-guardrails](../embabel-safety/embabel-guardrails/README.md) | 8898 | 输入/输出护栏（`UserInputGuardRail` / `AssistantMessageGuardRail`，CRITICAL 阻断） |
| [embabel-secure-tools](../embabel-safety/embabel-secure-tools/README.md) | 8924 | 工具安全（最小权限只读工具 + PII 输入/输出检测） |

## 建议阅读顺序

1. `embabel-guardrails` —— 先掌握护栏机制（校验、严重级别、阻断）
2. `embabel-secure-tools` —— 再看工程实践（最小权限 + PII + 敏感操作确认）

## 与相邻分类的边界

- **"模型能否调用某工具"是安全边界**：不给就不会被调用（见 `embabel-secure-tools`），
  比事后过滤可靠；**工具的实现安全**（沙箱）见 ② `embabel-file-tools`。
- **人工审批**属于交互手段（③ `embabel-hitl`），常与本章的"敏感操作"配合使用。
- **成本失控**（另一种"安全"）见 ⑦ `embabel-byok` 的成本上限，或待补的 `embabel-early-termination`。
- **评估**（③→⑥）用于验证质量，见 ⑥ `embabel-eval`。
