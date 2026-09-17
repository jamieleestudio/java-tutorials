# embabel-routing — 路由（分类后分派）

## 演示内容

Anthropic《Building Effective Agents》里的 **Routing**：先对输入**分类**，再交给**专门的后续处理**
（不同提示词/工具/模型）。好处是关注点分离——避免用一套提示词硬扛所有输入。

## 关键 API

| API | 作用 |
|---|---|
| `@Action(post = {"isRefund","isTech","isGeneral"})` | 分类动作声明"分类后可能让这些条件成立" |
| `@Condition(name = "isRefund")` | 判定条件（本示例同时接受英文/中文关键词） |
| `@Action(pre = {"isRefund"}) @AchievesGoal` | 专门处理器，只有条件成立才可能被选中 |

## 接口

```bash
# 命中 REFUND 通道
curl -G --data-urlencode "message=我买的商品坏了，想申请退款" http://localhost:8917/routing/ask
# 命中 TECH 通道
curl -G --data-urlencode "message=登录一直报 500 错误"        http://localhost:8917/routing/ask
# 命中 GENERAL 通道
curl -G --data-urlencode "message=你们的营业时间是什么"        http://localhost:8917/routing/ask
```

返回里带 `channel`，可直接看出走了哪条通道。

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-routing spring-boot:run
```

## 代码结构

- `RoutingAgent.java` — `classify` + 三个 `@Condition` + 三个 `@AchievesGoal` 处理器
- `RoutingController.java` — `GET /routing/ask`

## 要点

- 与 `embabel-multi-goal` 的区别：那里是**排序器**在多个目标间选择（LLM 排序）；
  这里是**条件**精确决定通道，语义确定、可预测。
- 分类结果用字符串（同时匹配中英文）而不是枚举：提示词方案下模型可能输出中文，
  这样更健壮。
- 文章提到的进阶用法：把"简单/常见问题"路由到**便宜的小模型**、"困难/罕见问题"路由到强模型——
  在 Embabel 里可用 `ai.withLlmByRole(...)` 实现（见 `embabel-multi-model`）。


## 附：复合条件（未单独建模块）

本模块用 `@Condition` + 单个谓词做路由。条件本身可以组合：

| API | 作用 |
|---|---|
| `AndCondition(a, b, ...)` | 全部满足 |
| `OrCondition(a, b, ...)` | 任一满足 |
| `NotCondition(c)` | 取反 |
| `ComputedBooleanCondition(name, evaluator)` | 用代码计算（`(blackboard, condition) -> Boolean`） |
| `UnknownCondition` | 显式表示"未知"，让规划器去做信息收集 |

`ComputedBooleanCondition` 是 `embabel-parallelization` 的 `aggregate` 内部在用的机制
（"所有分片都完成了吗"），所以它不只是"条件组合"，也是**自定义规划前提**的入口。
