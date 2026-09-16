# embabel-tree-of-thoughts — 思维树（分支 + 评分 + 剪枝）

## 演示内容

把"搜索"显式做出来，而不是让模型一次给答案：

1. **分支**：生成 3 个**互不相同**的解题思路
2. **评分**：模型作为评审给每个思路打 0~1 分（可行性 + 具体程度）
3. **展开最优**：只对得分最高的思路做 2 个改进版本（贪心扩展，控制成本）
4. **取全局最优**：返回最佳思路 + **完整搜索轨迹**（可解释）

## 关键 API / 做法

| 项 | 说明 |
|---|---|
| 循环 + `ai.withDefaultLlm().generateText(...)` | 分支生成与展开 |
| `ai.creating(Judge.class).fromPrompt(...)` | 结构化评分（score + reason） |
| `Comparator.comparingDouble(Candidate::score)` | 剪枝：只展开最优 |
| 返回 `explored` 列表 | 搜索轨迹可解释、可复盘 |

## 接口

```bash
curl -G --data-urlencode "problem=如何把大模型的响应延迟从 5 秒降到 1 秒以内？" http://localhost:8931/tot/ask
```

实测（节选）：

```
best = 1B草稿每步猜8token，大模型并行验证并批量接受。
explored（5 个候选）：
  [第1层] 0.25  利用用户输入停顿，边打字边预推理…
  [第1层] 0.55  用小型草稿模型先猜，大模型并行验证，加速解码。   ← 第1层最优
  [第1层] 0.35  在用户输入时预判问题并预生成候选答案…
  [第2层] 0.60  1B草稿每步猜8token，大模型并行验证并批量接受。   ← 全局最优
  [第2层] 0.58  多草稿模型并行猜，大模型树式验证剪枝，复用KV，超时回退。
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-tree-of-thoughts spring-boot:run
```

一次搜索约 10 次 LLM 调用（3 生成 + 3 评分 + 2 展开 + 2 评分），30~60s。

## 代码结构

- `TotAgent.java` — 分支 / 评分 / 展开 / 取最优（含 `BRANCH`、`REFINEMENTS` 常量）
- `Candidate` / `Judge` / `TotResult` — 候选、评分与结果结构
- `TotController.java` — `GET /tot/ask`

## 要点

- **为什么有用**：复杂问题里模型的"第一反应"常常不是最优；显式分支 + 评分 + 剪枝能显著提升质量，
  代价是更多调用。可用 `BRANCH` / `REFINEMENTS` 调成本与质量的平衡。
- vs **`embabel-parallelization`（sectioning）**：分片是"同一任务的独立子任务"；
  这里是"同一问题的多个**竞争**方案"。
- vs **`embabel-refinement`**：refinement 沿一条路迭代改进；ToT 先分叉、再按评分选路。
- 生产上可把评分换成多评审员投票（见 `embabel-parallelization` 的 voting）以减少评分波动。
