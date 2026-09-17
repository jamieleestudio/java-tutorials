# agentscope-tools — 工具调用

## 演示内容

用 `@Tool` + `@ToolParam` 注解声明工具，注册到 `Toolkit`，模型在 ReAct 循环中自主调用。

```bash
curl -G --data-urlencode "message=帮我查一下订单 A1002 的状态，然后算一下打 8 折是多少钱" \
     http://localhost:9101/tools/ask
```

实测（模型自主调用了两个工具并综合结果）：

```
订单 A1002：已发货，金额 ¥899，下单时间 2026-08-15
8 折计算：原价 ¥899 → 折后价 ¥719.20（优惠 ¥179.80）
```

## 关键 API

| API | 作用 |
|---|---|
| `@Tool(name, description)` | 注解在方法上，声明工具（还有 `readOnly`/`concurrencySafe`/`dangerousFiles` 等安全属性） |
| `@ToolParam(name, description, required)` | 注解在参数上，声明工具参数（**`name` 必填，无默认值**） |
| `Toolkit toolkit = new Toolkit(); toolkit.registerTool(obj)` | 注册工具对象（反射提取 `@Tool` 方法） |
| `HarnessAgent.builder().toolkit(toolkit)` | 把 Toolkit 注入 Agent |

## 与 Embabel 的对照

| | Embabel | AgentScope |
|---|---|---|
| 注解 | `@LlmTool(category, description)` + `@LlmTool.Param` | `@Tool(name, description)` + `@ToolParam(name, description)` |
| 注册 | 框架自动扫描 `@LlmTool` | `Toolkit.registerTool(obj)` 手动注册 |
| 安全属性 | 无 | `readOnly`/`concurrencySafe`/`dangerousFiles`/`dangerousDirectories` |
| 工具分组 | `@UnfoldingTools` 折叠成门面 | `ToolGroup` / `ToolGroupManager` |

## 运行

```bash
cd ai
mvn -pl :agentscope-tools spring-boot:run
```

## 代码结构

- `OrderQueryTools.java` — 3 个 `@Tool` 方法（查询订单/获取时间/计算折扣）
- `ToolsAgent.java` — `Toolkit` + `HarnessAgent.builder().toolkit(...)`
- `ToolsController.java` — `GET /tools/ask`

## 要点

- **`@ToolParam` 的 `name` 是必填的**（没有默认值），编译时会报错——这是和 Embabel 的 `@LlmTool.Param`（name 可选）的一个区别。
- AgentScope 的 `@Tool` 带了安全属性（`readOnly`/`concurrencySafe`/`dangerousFiles`），这些在后面的 Permission 模块里会被 `PermissionEngine` 使用——Embabel 没有等价物。
- 模型在 ReAct 循环中**自主决定**调用哪个工具、按什么顺序——不需要像 Embabel 那样由 GOAP 规划器推导。