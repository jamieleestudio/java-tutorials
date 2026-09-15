# embabel-eval — 评估（eval harness）

## 演示内容

**数据集 → 跑被测系统 → LLM 评审打分 → 报告**。单元测试只能验证结构与调用，
而 Agent 的输出是自然语言，必须**按语义评分**——评估集是 Agent 从"能跑"到"可靠"的关键。

## 关键 API / 做法

| 项 | 说明 |
|---|---|
| 用例 = 问题 + **判定标准**（criterion） | 标准写清楚，评审才稳定（见 `Dataset.java`） |
| 被测系统逐条作答 | 一个 PromptRunner 调用 |
| **LLM 评审** | `ai.creating(Judge.class).fromPrompt(...)` → `score` + `reason` |
| 报告 | 总数 / 通过数 / 平均分 / 逐条明细 |

## 接口

```bash
curl http://localhost:8923/eval/run
```

实测返回（节选）：

```json
{"total":4,"passed":2,"averageScore":0.775,
 "results":[{"id":"case-1","score":0.6,"passed":false,
   "reason":"...未提及'可复用'这一关键属性，因此只满足部分判定要求..."}]}
```

注意 `reason` 的价值：它不只给分，还**指出哪里不达标**——正是改进提示词的依据。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-eval spring-boot:run
```

一次评估 = `用例数 × 2` 次 LLM 调用（作答 + 评审），4 条用例约 30~60s。

## 代码结构

- `Dataset.java` — 评估集（问题 + 判定标准）
- `EvalAgent.java` — 逐条作答 + LLM 评审 + 汇总
- `Judge` / `CaseResult` / `EvalReport` — 评审与报告结构
- `EvalController.java` — `GET /eval/run`

## 要点

- **用途是回归**：改提示词 / 换模型 / 调参数前后各跑一次，比较通过率与均分。
- 评审要用**结构化输出**（`Judge`），并且标准要可判定（"必须提到 X"比"回答得好"稳定得多）。
- 评审本身可能不稳定 → 生产上可多评审员投票（见 `embabel-parallelization` 的 voting），
  或固定随机性、多次取中位数。
- 与 `embabel-testing` 的分工：testing 验证"结构与逻辑"（离线、毫秒级）；
  eval 验证"输出质量"（需要真实 LLM、秒级）。两者互补，都可进 CI。
