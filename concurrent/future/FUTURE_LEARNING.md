# Java Future 学习指南

## 1. Future 简介

`Future` 是 Java 5 引入的接口，位于 `java.util.concurrent` 包中。它代表一个异步计算的结果。提供了一种检查计算是否完成、等待计算完成以及检索计算结果的方法。

简单来说，`Future` 就像去餐厅吃饭时拿到的“小票”。你点了餐（提交任务），厨房在做（异步执行），你可以去做别的事（玩手机），等好了凭小票取餐（获取结果）。

## 2. 基础用法

核心方法：
- `get()`: 获取结果。如果计算还没完成，会阻塞等待。
- `get(long timeout, TimeUnit unit)`: 获取结果，但只等待指定时间。超时抛出 `TimeoutException`。
- `isDone()`: 判断任务是否完成。
- `cancel(boolean mayInterruptIfRunning)`: 取消任务。
- `isCancelled()`: 判断任务是否被取消。

## 3. 常用实现

### 3.1 FutureTask
`FutureTask` 是 `Future` 的一个基本实现，同时实现了 `Runnable` 接口。
- 可以直接被 `Thread` 执行。
- 也可以提交给 `ExecutorService` 执行。

### 3.2 CompletableFuture (Java 8+)
这是 `Future` 的增强版，也是现代 Java 异步编程的首选。
- **支持链式调用**：任务完成后自动触发下一个任务 (`thenApply`, `thenAccept`)。
- **组合多个 Future**：等待所有完成 (`allOf`) 或任意一个完成 (`anyOf`)。
- **异常处理**：提供了 `exceptionally`, `handle` 等优雅的异常处理机制。
- **无需阻塞**：可以通过回调方式处理结果，真正实现非阻塞。

## 4. 常用场景

1.  **耗时任务异步化**：如发送邮件、生成报表、复杂的数学计算，避免阻塞主线程。
2.  **并行计算**：比如一个页面需要聚合用户信息、订单信息、积分信息。可以并发发出3个请求，最后再组装结果，大大降低总耗时。
3.  **超时控制**：利用 `get(timeout)` 方法，防止某个依赖服务挂死导致整个系统卡顿。

## 5. 容易犯错的坑 (Pitfalls)

1.  **滥用 `get()` 导致阻塞**
    - `Future.get()` 是阻塞的。如果你提交了异步任务，却立刻调用 `get()`，那和同步调用没区别，还多了线程切换的开销。
    - **解决**：尽量晚一点调用 `get()`，或者使用 `CompletableFuture` 的回调机制。

2.  **忽略异常**
    - 异步任务中的异常如果不处理，可能会被“吞掉”（特别是 `submit` 后不调用 `get` 的情况，虽然 `get` 会抛出 `ExecutionException`，但不调就不知道）。
    - **解决**：务必处理 `Future.get()` 抛出的异常，或者在 `CompletableFuture` 中使用 `exceptionally`。

3.  **`cancel` 的误解**
    - `cancel(true)` 只是给执行线程发一个中断信号（interrupt）。如果你的任务代码里不响应中断（比如没有检查 `Thread.currentThread().isInterrupted()`），任务是停不下来的。
    - **解决**：编写任务代码时，耗时循环或阻塞操作要考虑响应中断。

4.  **线程池枯竭**
    - 如果大量使用 `Future` 等待结果（特别是嵌套使用），可能会导致线程池线程耗尽，引发死锁（Pool Induced Deadlock）。
    - **解决**：避免在任务内部等待另一个提交到同线程池的任务结果；合理配置线程池大小。

## 6. 代码示例说明

请查看 `src/main/java/com/yineng/bpe/future` 目录下的示例代码：
- `BasicFutureDemo.java`: 演示最基本的 `ExecutorService` + `Future` 用法。
- `CompletableFutureDemo.java`: 演示 `CompletableFuture` 的链式调用和组合用法。
- `FuturePitfallDemo.java`: 演示常见的 `cancel` 无效和 `get` 阻塞问题。
