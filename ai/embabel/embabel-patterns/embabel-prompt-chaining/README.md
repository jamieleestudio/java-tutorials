# embabel-prompt-chaining — 链式提示 + 关卡（gate）

## 演示内容

Anthropic《Building Effective Agents》里的 **Prompt chaining**：把任务拆成固定顺序的步骤，
每步处理上一步的输出，并在中间插入**程序化检查（gate）**——不通过就停下，而不是继续跑偏。

链：`UserInput -> Outline -> GateResult -> Article`

## 关键 API

| API | 作用 |
|---|---|
| `@Action(post = {"outlineAccepted"})` | 关卡动作声明"我可能让该条件成立" |
| `@Condition(name = "outlineAccepted")` | 关卡判定（真正的布尔逻辑，由规划器求值） |
| `@Action(pre = {"outlineAccepted"})` | 只有条件成立才允许进入下一步 |
| `ai.evaluateCondition(条件, 上下文, 阈值)` | LLM 作为判定器（也可换成规则判断） |

## 接口

```bash
curl -G --data-urlencode "message=为什么要给 Agent 做类型化建模？" http://localhost:8916/chaining/write
```

返回：

```json
{"status":"COMPLETED","gate":"passed","article":{"title":"...","content":"..."}}
```

若关卡不通过，进程会停住，返回 `{"status":"STUCK","gate":"rejected","reason":"..."}`。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-prompt-chaining spring-boot:run
```

## 代码结构

- `WritingAgent.java` — `outline`（步骤 1）→ `check`（关卡）→ `write`（步骤 2，带 `pre`）
- `ChainingController.java` — 返回"走完"或"被拦截"两种情况

## 要点（踩坑记录）

- **`@Condition` 必须在某个动作的 `post` 里声明**，否则规划器无法把它当作可达效果，
  进程会直接 `STUCK`（我第一次没写 `post`，就是这个现象）。
  语义是："这个动作**可能**让该条件成立"，条件**最终是否成立由规划器真正求值**决定——
  所以关卡为假时，通往目标的路不存在，链条自然停止。
- 关卡用 `ai.evaluateCondition(...)` 是 LLM 判定；生产上也可以换成纯规则/校验器（更快更稳）。
- 与 `embabel-planning` 的区别：planning 演示"多步规划"，本模块演示"链式步骤 + 关卡拦截"。
- 文章原话的适用场景：任务能干净拆成固定子任务、愿意用延迟换准确率。
