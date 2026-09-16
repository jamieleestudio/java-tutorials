# embabel-tool-chaining — 工具链式展开（基于 artifacts）

## 演示内容

**工具产出领域对象 → 该对象的专属工具自动解锁。** 一行代码：

```java
ai.withDefaultLlm()
  .withToolObject(new OrderTools())        // 第一批工具：只有 searchOrder
  .withToolChainingFrom(Order.class)       // 解锁条件：黑板上出现 Order 实例
  .generateText("...");
```

实测流程（日志）：

```
starting tool loop [applyDiscount, currentStatus, searchOrder] max=20
calling tool searchOrder({"orderId": "A1001"})     ← 第一批工具
calling tool applyDiscount({"rate": 0.9})          ← 解锁后才可用
calling tool currentStatus({})                     ← 解锁后才可用
tool loop completed after 4 iterations
```

`Order` 上的 `@LlmTool` 方法**一开始对模型不可用**，直到某个工具真的返回了一个 `Order`。

## 关键 API

| API | 作用 |
|---|---|
| `PromptRunner.withToolChainingFrom(Class<T>)` | **核心**：一旦黑板上有单个 `T` 实例，就把 `T` 上的 `@LlmTool` 方法暴露为工具 |
| `withToolChainingFrom(Class<T>, DomainToolPredicate<T>)` | 带谓词（只对满足条件的实例解锁） |
| `withToolChainingFromAny()` | 自动发现：任何返回的对象都参与解锁 |
| `Tool.Result.withArtifact(text, artifact)` | 工具返回值里携带**强类型产物** |
| `Tool.publishToBlackboard(tool)` / `(tool, Class<T>)` / `(+filter, +transform)` | 显式把产物落到当前进程黑板 |
| `Tool.sinkArtifacts(tool, Class<T>, sink, filter, transform)` | 显式把产物送到自定义 sink |
| `ArtifactSink` / `BlackboardSink` / `ListSink` / `CompositeSink` | sink 抽象：落黑板 / 收进列表 / 分发到多个 sink |
| `@LlmTool` / `@LlmTool.Param` | 方法即工具；**返回非 String 对象时会自动包成 artifact** |

> **机制的关键**（框架源码 `ToolCallSupport.convertResult`）：
> 方法型工具返回非 `String` 时，会被自动包装成 `Tool.Result.withArtifact(json, 对象)`，
> 源码注释原话是 *"so that ArtifactSinkingTool can capture it for tool chaining"*。
> 所以 `searchOrder` 只要返回 `Order` 对象，链式解锁就自动成立。

## 接口与实测

```bash
# 主演示：查订单 → 自动解锁该订单的操作 → 打折
curl -G --data-urlencode "message=查一下订单 A1001，然后给它打 9 折" \
     http://localhost:8936/tool-chaining/ask

# artifacts 的显式用法：类型过滤 + 谓词过滤 + 转换 + 多 sink
curl http://localhost:8936/artifacts/sink
```

实测 `/tool-chaining/ask`：

```json
{"content":"完成了 ✅\n- **订单号**：A1001\n- **查询结果**：状态为「已支付」，原折扣 0.0\n- **已应用折扣**：0.9（9 折）\n- **当前状态**：已支付，折扣 = 0.90\n\n⚠️ 一点提醒：该订单目前状态是「已支付」，通常已支付订单再打折可能涉及退款或金额调整…"}
```

实测 `/artifacts/sink`（原始 artifact 有 3 个元素，最终只捕获 1 条）：

```json
{"toolContent":"命中 3 条",
 "capturedByListSink":["A1001/已支付"],
 "auditLog":["审计：A1001/已支付"],
 "note":"原始 artifact 是 3 个元素（2 个 Order + 1 个 String）；A1002 因状态为「待支付」被谓词过滤，String 因类型不符被过滤，最终只捕获 1 条"}
```

对应的 sink 写法（`/artifacts/sink` 端点）：

```java
Tool wrapped = Tool.sinkArtifacts(
        raw,
        Order.class,                              // 1) 类型过滤：只收 Order
        new CompositeSink(listSink, auditSink),   // 4) 分发到多个 sink
        order -> "已支付".equals(order.getStatus()), // 2) 谓词过滤
        order -> order.getId() + "/" + order.getStatus()); // 3) 转换
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-tool-chaining spring-boot:run
```

## 代码结构

- `Order.java` — 领域对象，**自带 `@LlmTool` 方法**（`applyDiscount` / `currentStatus`）
- `OrderTools.java` — 第一批工具：`searchOrder` **返回 `Order` 对象**（不是字符串）
- `ToolChainingAgent.java` — `withToolObject(...)` + `withToolChainingFrom(Order.class)`
- `ToolChainingController.java` — `/tool-chaining/ask`、`/artifacts/sink`
- `Reply.java` — 目标类型

## 要点

- **为什么有用**：工具很多时不必一次性全塞进提示词。"**领域对象出现 = 解锁条件**"，
  工具集随上下文增长。比按 `category` 手动展开更"数据驱动"。
- 与 `embabel-tools-advanced` 的分工：那里是**渐进式工具**（模型主动展开一个门面，
  按 `category` 决定展开哪组）；这里是**自动解锁**（不用模型请求，数据一到就解锁）。
  两者可叠加使用。
- 与 `embabel-state-machine` 的分工：那里按**状态**收敛工具；这里按**数据**收敛工具。
- 观察到的细节：tool loop 启动时 3 个工具名**都已经在列表里**——
  框架会先注册**占位工具**（让模型知道它们存在），实例出现后再替换为真正可用的工具。
  所以不要指望"日志里看不到名字"来验证解锁；看**调用是否成功**更可靠。
- 生产建议：用 `withToolChainingFrom(Class, predicate)` 加谓词，
  避免"任意一个该类型对象出现就解锁全部危险操作"。
