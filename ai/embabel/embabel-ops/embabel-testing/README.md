# embabel-testing — 无需 API Key 的确定性测试

## 演示内容

三种**离线**测试方式（不联网、不消耗额度、毫秒级）：

1. **规划校验**：用 `GoapPathToCompletionValidator` 检查"目标是否可达"
2. **动作单测**：用 Mockito 模拟 `Ai` / `PromptRunner`，并用 `ArgumentCaptor` 断言真实提示词
3. **上下文启动**：`@SpringBootTest` 离线启动，断言模型注册与 Agent 扫描

## 关键 API

| API | 作用 |
|---|---|
| `AgentScopeBuilder.fromInstance(agent)` | 从 `@Agent`/`@EmbabelComponent` 实例构建 `AgentScope` |
| `GoapPathToCompletionValidator` | 分析动作/目标/类型依赖，判断能否规划到目标 |
| `ValidationResult.isValid()` / `getErrors()` | 校验结果 |
| `Mockito` + `ArgumentCaptor` | 模拟 LLM 并捕获提示词 |
| `@SpringBootTest`（MOCK，不占端口） | 离线启动容器做冒烟断言 |

> 框架还提供 `ScriptedLlmOperations`（脚本化 LLM 调用：`respond(...)` / `callTool(...)` / `returnObject(...)`）、
> `FakePromptRunner`、`DummyObjectCreatingLlmOperations`，以及 `embabel.agent.platform.test.mock-mode` 配置，
> 适合更细粒度的集成测试（本模块从最实用的三种入手）。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-testing test
```

预期：4 个测试全部通过。

## 代码结构

- `OutlineAgent.java` — 被测的最小 Agent（1 动作 + 1 目标）
- `src/test/.../AgentPlanValidationTest.java` — 正例（可达）+ 反例（循环依赖 → `NO_STARTING_ACTION`）
- `src/test/.../OutlineAgentTest.java` — Mockito 单测 + 提示词断言
- `src/test/.../ContextBootTest.java` — 离线启动断言

## 要点（踩坑记录）

- **"输入没有动作产出"不等于不可达**：校验器会把这种输入当作**外部输入**（假定可用）。
  真正会失败的典型是**循环依赖**——所有动作都依赖别人的产出，没有起始动作，
  报错 `NO_STARTING_ACTION`。理解这一点才能写出有效的反例测试。
- 单测里 mock `PromptRunner.Creating<T>` 即可覆盖 `creating(...).fromPrompt(...)` 这条链路。
- `@SpringBootTest` 用占位 api-key 也能启动（不会发起调用），适合做配置回归。
