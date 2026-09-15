# Embabel 内部知识文档（示例）

本文档用于演示 Embabel 的 `LlmReference`：把文档内容作为参考资料注入提示词，
让模型"依据资料"回答。文档内容会被放进提示词，因此适合**小体量**知识。

## 核心概念

- **Agent**：一个围绕目标运作的智能体，用 `@Agent` 标注。
- **Action**：可复用的动作，用 `@Action` 标注；输入来自共享上下文，返回即产出。
- **Goal**：目标，用 `@AchievesGoal` 声明某个动作达成它。
- **Blackboard**：共享上下文，动作之间通过它按类型传递对象。
- **Planner**：规划器，默认 GOAP（从目标反向规划动作链）；另有 UTILITY / HYBRID / SUPERVISOR。

## 常用能力

- 工具调用：`@LlmTool` 方法可被模型调用。
- 结构化输出：`PromptRunner.creating(T.class)` 直接产出强类型对象。
- 人机协同：`WaitFor.confirmation(...)` / `WaitFor.formSubmission(...)` 暂停并等待人工。
- 流式输出：`PromptRunner.streaming()` 配合 SSE。
- 推理提取：`Thinking.withExtraction()`（要求模型把推理写在 `<think>` 标签中）。

## 本示例的约定

回答用户问题时，应只依据本参考文档与术语表，并在答案中标注引用的资料名称。
若资料中没有答案，应明确说明"资料中未提及"，不要编造。
