# ⑥ 工程化（embabel-ops）

## 这一章解决什么

从"能跑的 Demo"到"能上生产的系统"缺的那部分：**看得见**（观测/成本）、**花不超**（预算/熔断）、
**测得住**（测试/评估）、**存得下**（持久化）。

## 模块清单

| 模块 | 端口 | 主题 |
|---|---|---|
| [embabel-observability](../embabel-ops/embabel-observability/README.md) | 8903 | 事件监听 + 成本/Token 统计（`AgenticEventListener`、`totalCost()`） |
| [embabel-budget](../embabel-ops/embabel-budget/README.md) | 8934 | 运行预算与熔断（`Budget` / `EarlyTerminationPolicy` / 限速） |
| [embabel-stuck-handler](../embabel-ops/embabel-stuck-handler/README.md) | 8935 | 卡住兜底（`StuckHandler`：补前提后 `REPLAN`） |
| [embabel-otel](../embabel-ops/embabel-otel/README.md) | 8942 | 可观测性接入点（`AgentInstrumentation` → Micrometer/OTel span） |
| [embabel-testing](../embabel-ops/embabel-testing/README.md) | 8904 | 无需 API Key 的确定性测试（规划校验 / Mockito / 上下文启动） |
| [embabel-persistence](../embabel-ops/embabel-persistence/README.md) | 8911 | 上下文持久化到 Postgres（类型保真 JSON；需 Docker） |
| [embabel-eval](../embabel-ops/embabel-eval/README.md) | 8923 | 评估 harness（数据集 + LLM 评审 + 通过率报告） |

## 建议阅读顺序

1. `embabel-testing` —— 先能"离线、秒级"验证结构与逻辑
2. `embabel-eval` —— 再验证"输出质量"（需要真实 LLM）
3. `embabel-observability` —— 上线前装上眼睛（每步耗时、模型、成本）
4. `embabel-budget` —— 光看见不够，还要能**掐断**（动作/token/成本三重上限）
5. `embabel-stuck-handler` —— 掐断之外的另一面：**卡住时怎么补救**（补前提后重规划）
6. `embabel-persistence` —— 需要跨重启保留状态时再接数据库

## 与相邻分类的边界

- **testing vs eval**：前者验证**结构与逻辑**（mock LLM、毫秒级）；后者验证**输出质量**
  （真实 LLM、语义评分）。两者互补，都可进 CI。
- **observability vs budget vs stuck-handler**：依次是**看得见**（花了多少）、**花不超**（超了终止）、
  **卡得住**（卡了补救）。生产上三个都要。
- **stuck-handler vs replanning（⑧）**：前者是规划器**自己**找不到路时的钩子；
  后者是工具**主动**报告失败后换路。触发时机不同，可同时使用。
- **observability 是自建监听器**；要接 Micrometer/OpenTelemetry 待补 `embabel-otel`。
- **持久化的边界**：只做 `ContextRepository`（用户/会话状态）；
  `AgentProcess`（含黑板/规划器）不适合直接序列化，生产上走事件溯源。
