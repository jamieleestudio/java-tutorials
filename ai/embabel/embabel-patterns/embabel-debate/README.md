# embabel-debate — 多 Agent 辩论

## 演示内容

让同一问题被**多个对立/不同视角**分别论证，再由"裁判"综合——相比单次调用，
能暴露单视角容易忽略的反例与风险，结论更稳。

流程：`支持 / 反对 / 中立务实` 三方各出论点 → 裁判给出**有取舍的结论**（明确适用条件）

## 关键 API / 做法

| 项 | 说明 |
|---|---|
| 多个独立 LLM 调用（不同 persona 提示词） | 每个立场一次调用，互不影响 |
| `ai.creating(Judge.class)`（可选） | 需要结构化评分时用，本示例直接用文本论点 |
| 裁判调用 | 汇总三方论点，要求"给出条件化结论"而不是和稀泥 |

## 接口

```bash
curl -G --data-urlencode "topic=初创团队是否应该直接上微服务架构？" http://localhost:8930/debate/ask
```

返回结构：`{"topic":..., "arguments":[{"stance":"坚决支持...","points":"..."}...], "conclusion":"..."}`

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-debate spring-boot:run
```

## 代码结构

- `DebateAgent.java` — 三个立场循环调用 + 裁判综合
- `Argument` / `DebateResult` — 论点与结论结构
- `DebateController.java` — `GET /debate/ask`

## 要点（与相近模式的区别）

- vs **`embabel-parallelization`（voting）**：投票是"同一问题多票取多数"；
  辩论是"**不同立场互相反驳**"，信息量更大。
- vs **`embabel-workflows`（Consensus）**：共识是"多个模型给同一答案再合并"；
  辩论强调**对立视角**，适合有争议的决策问题。
- 成本是 `立场数 + 1` 次调用；立场数可按问题复杂度调整。
- 想更进一步可加"多轮交锋"（第二轮让各方针对对方论点反驳）与"结构化评分"（让裁判给各论点打分）。
