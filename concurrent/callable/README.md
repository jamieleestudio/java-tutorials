# Callable/Future 学习与实战

- Callable 表示可返回结果并可能抛出异常的并发任务。
- Future 表示任务的未来结果，支持轮询状态、阻塞获取、取消。
- 适合需要拿到计算结果的并发场景，相比 Runnable 可携带返回值与异常。

## 基础概念
- Callable：函数式接口，`V call() throws Exception`。
- Future：任务句柄，`get()` 阻塞获取结果，`cancel()` 取消任务。
- FutureTask：同时实现 Runnable 和 Future，可自行启动线程或交给线程池。
- CompletionService：按完成先后获取结果，适合“谁先完成先处理”。

## 代码示例
- 基础 Callable 提交与取值：[BasicCallableExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/callable/src/main/java/com/yineng/bpe/BasicCallableExample.java)
- 批量任务 `invokeAll`：[InvokeAllExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/callable/src/main/java/com/yineng/bpe/InvokeAllExample.java)
- 超时与取消：[TimeoutCancelExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/callable/src/main/java/com/yineng/bpe/TimeoutCancelExample.java)
- 异常传播与处理：[ExceptionPropagationExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/callable/src/main/java/com/yineng/bpe/ExceptionPropagationExample.java)
- 按完成顺序处理结果：[CompletionServiceExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/callable/src/main/java/com/yineng/bpe/CompletionServiceExample.java)
- FutureTask 使用：[FutureTaskExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/callable/src/main/java/com/yineng/bpe/FutureTaskExample.java)
- 实战：并发抓取多个 URL：[WebFetchExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/callable/src/main/java/com/yineng/bpe/WebFetchExample.java)
- 统一入口运行示例：[Main.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/callable/src/main/java/com/yineng/bpe/Main.java)

## 常见场景
- 批量并发计算后汇总：使用 `invokeAll` 或 `CompletionService`。
- 远程调用聚合：为每个下游调用封装为 Callable，统一超时与取消策略。
- I/O 并发爬取：限制线程池大小，设置连接与读取超时，汇总成功与失败。
- 预热和缓存构建：后台并发计算，主线程通过 Future 获取结果。

## 最容易踩的坑
- 无界阻塞：`get()` 无超时导致主线程长时间阻塞，建议使用带超时的 `get`。
- 取消无效：未处理中断，`cancel(true)` 不生效，应在任务中响应中断。
- 异常吞噬：异常以 `ExecutionException` 包装，需读取 `getCause()`。
- 线程池泄漏：忘记 `shutdown`/`shutdownNow`，进程无法退出。
- 死锁：单线程池中任务间相互等待导致阻塞，应合理选择线程池大小。
- 结果处理顺序：按提交顺序 `get` 效率低，优先使用 `CompletionService`。
- 超时与取消协作：`invokeAll(timeout)` 返回的未完成任务已被取消，需判断 `isCancelled()`。
- 重计算浪费：结果未缓存或未复用，重复提交相同任务增加资源消耗。

## 选择建议
- 只需要结果且简单：Callable + Future。
- 需要先完成先处理：CompletionService。
- 需要手动控制启动：FutureTask。
- 需要组合、管道化：优先考虑 `CompletableFuture`（本模块以 Callable/Future 为主）。

## 运行方式
- 从项目根构建：`mvn -q -DskipTests compile`
- 运行示例入口：执行 `Main.main`，观察控制台输出。
