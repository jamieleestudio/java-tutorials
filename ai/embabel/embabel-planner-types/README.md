# embabel-planner-types — 规划器类型对比（GOAP / UTILITY）

## 演示内容

同一个框架下，用 `@Agent(planner = ...)` 切换规划策略：

- **GOAP**（默认）：从目标反向规划，**必须有目标**，动作顺序由类型链条推导
- **UTILITY**：不做目标反推，每轮挑"净价值最大"（value − cost）的可用动作，**不要求有目标**

## 关键 API

| API | 作用 |
|---|---|
| `@Agent(planner = PlannerType.GOAP / UTILITY)` | 指定规划器（还支持 `HYBRID`、`SUPERVISOR`） |
| `@Action(value = ..., cost = ...)` | 动作的静态价值/代价 |
| `@Action(valueMethod = "...")` | 引用一个 `@Cost` 方法动态计算价值 |
| `@Cost(name = "...")` | 规划时计算 cost/value；**所有领域参数必须可空** |

## 接口

```bash
# GOAP：TalkingPoints -> GoapAnswer
curl -G --data-urlencode "message=为什么要给 Agent 做类型化建模？" http://localhost:8900/planner/goap

# UTILITY：按动态 value 选动作
curl -G --data-urlencode "message=这个回答需要比较长，请详细解释一下什么是 utility planning" http://localhost:8900/planner/utility
```

两者返回中都会带回 `planner` 字段（`GOAP` / `UTILITY`）。

## 运行

```bash
cd ai/embabel
mvn -pl embabel-planner-types spring-boot:run
```

## 代码结构

- `GoapPlannerAgent.java` — `@Agent(planner = GOAP)`，两个动作串成类型链
- `UtilityPlannerAgent.java` — `@Agent(planner = UTILITY)`，`@Cost adviceValue(...)` 动态价值
- `PlannerController.java` — 两个对比端点

## 要点

- `@Cost` 方法的领域对象参数**必须可空**：对象不在黑板上时会传 `null`（方法也可接收 `Blackboard`）。
- UTILITY/HYBRID 用 value/cost 做决策；GOAP 只用 pre/post 条件做规划，所以
  `value`/`valueMethod` 在 GOAP 下没有意义。
- `SUPERVISOR` 适合"主管调度多个子 Agent"的场景，`HYBRID` 则可把"持续探索型动作"与"终止型目标"结合。
