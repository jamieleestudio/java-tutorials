# embabel-trigger — 反应式触发

## 演示内容

`@Action(trigger = X.class)`：动作**只在类型 X 刚被放进黑板时**才执行。
trigger 是**额外的前置条件**，与参数类型前置条件叠加。

## 关键 API

| API | 作用 |
|---|---|
| `@Action(trigger = UserInput.class)` | 仅当 `UserInput` 是黑板上最后一个对象时才允许执行 |
| `@Action(trigger = ..., canRerun = ...)` | trigger 与重跑策略可组合 |

## 接口

```bash
curl -G --data-urlencode "message=什么是反应式触发？" http://localhost:8913/trigger/ask
```

## 运行

```bash
cd ai/embabel
mvn -pl :embabel-trigger spring-boot:run
```

## 代码结构

- `TriggerAgent.java` — `classify`（演示用的旁路动作，不参与目标）+ `respond`（带 trigger 的目标动作）
- `TriggerController.java` — `GET /trigger/ask`

## 要点（为什么需要 trigger）

- 在**长生命周期**进程里（常驻聊天机器人、多轮会话），黑板上会长期留着用户输入等对象。
  没有 trigger 时，只要动作的输入类型还在，规划器就可能反复选中它；加上 trigger 后，
  动作只在"**新输入刚到达**"时触发一次，避免重复执行。
- trigger 类型必须是**最后一个**被加入黑板的类型——所以一个动作若"先产出了别的东西"，
  后续带 trigger 的动作就**不会再触发**。设计动作顺序时要注意这一点。
- 与 `ChatTrigger` 的区别：`ChatTrigger` 用于让系统**主动发起**对话（定时/事件驱动），
  见 `AgentProcessChatbot`/`ChatSession` 的 `onTrigger`；本模块演示的是**动作级**的触发约束。
