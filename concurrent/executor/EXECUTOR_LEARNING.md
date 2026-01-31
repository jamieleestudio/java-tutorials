# Java Executor 框架学习指南

## 1. 简介
Executor 框架是 Java 5 引入的，用于将**任务提交**（Submission）与**任务执行**（Execution）解耦。
在此之前，我们通常直接 `new Thread(() -> { ... }).start()`，这会导致：
- 线程创建/销毁开销大。
- 无法控制并发数，可能耗尽系统资源。
- 缺乏统一的管理（如定时、取消、异常处理）。

Executor 框架的核心是 **线程池（Thread Pool）**。

## 2. 核心接口

*   `Executor`: 最顶层接口，只有一个 `execute(Runnable)` 方法。
*   `ExecutorService`: 扩展了 `Executor`，增加了生命周期管理（`shutdown`）、任务提交返回 Future（`submit`）等方法。
*   `ScheduledExecutorService`: 支持定时或周期性执行任务。
*   `ThreadPoolExecutor`: 线程池的核心实现类。

## 3. ThreadPoolExecutor 详解 (核心知识点)

这是创建线程池最原始也最推荐的方式。构造函数包含 7 个参数：

```java
public ThreadPoolExecutor(
    int corePoolSize,    // 1. 核心线程数：即使空闲也保留的线程数
    int maximumPoolSize, // 2. 最大线程数：队列满时，允许扩容到的最大线程数
    long keepAliveTime,  // 3. 存活时间：超过 corePoolSize 的线程，空闲多久被回收
    TimeUnit unit,       // 4. 时间单位
    BlockingQueue<Runnable> workQueue, // 5. 任务队列：等待执行的任务放在这里
    ThreadFactory threadFactory,       // 6. 线程工厂：用于创建新线程（可自定义线程名）
    RejectedExecutionHandler handler   // 7. 拒绝策略：队列满且线程数达到最大时，如何拒绝新任务
)
```

### 3.1 拒绝策略 (RejectedExecutionHandler)
当线程池忙碌且队列已满时，触发拒绝策略：
1.  `AbortPolicy` (默认): 抛出 `RejectedExecutionException` 异常。
2.  `CallerRunsPolicy`: 由提交任务的线程（通常是主线程）直接执行该任务。**（起到削峰填谷作用，减缓提交速度）**
3.  `DiscardPolicy`: 默默丢弃任务，不抛异常。
4.  `DiscardOldestPolicy`: 丢弃队列里最老的任务，尝试再次提交当前任务。

## 4. 常见的内置线程池 (Executors 工厂类)

虽然 `Executors` 提供了便捷方法，但在生产环境中**不建议直接使用**（阿里巴巴开发手册强制规定），原因如下：

| 方法 | 描述 | 风险 (OOM Risk) |
| :--- | :--- | :--- |
| `newFixedThreadPool(n)` | 固定大小线程池 | 使用无界队列 `LinkedBlockingQueue`，任务堆积可能导致 OOM。 |
| `newSingleThreadExecutor()` | 单线程池 | 使用无界队列，同样可能导致 OOM。 |
| `newCachedThreadPool()` | 可缓存线程池 | 最大线程数是 `Integer.MAX_VALUE`，并发极高时可能创建过多线程耗尽资源。 |
| `newScheduledThreadPool(n)` | 定时任务线程池 | 使用 `DelayedWorkQueue`，也是无界队列。 |

**最佳实践**：始终使用 `new ThreadPoolExecutor(...)` 手动创建线程池，明确指定队列大小和拒绝策略。

## 5. 生命周期管理

*   `shutdown()`: 平滑关闭。不接收新任务，但会把队列里已有的任务执行完。
*   `shutdownNow()`: 暴力关闭。尝试中断正在执行的任务，并返回等待队列中的任务列表。
*   `awaitTermination(timeout, unit)`: 阻塞等待所有任务关闭。

## 6. 代码示例说明

请查看 `src/main/java/com/yineng/bpe/executor` 目录下的示例代码：

*   `BasicPoolDemo.java`: 演示 `Executors` 便捷方法的使用（仅用于测试/学习）。
*   `CustomThreadPoolDemo.java`: **重点**，演示如何自定义 `ThreadPoolExecutor`，配置有界队列和拒绝策略。
*   `ScheduledDemo.java`: 演示定时任务。
*   `LifecycleDemo.java`: 演示如何优雅地关闭线程池。

## 7. 容易犯错的坑

1.  **局部变量线程池**：在方法内部创建线程池，每次调用都创建一个新的，用完不关，导致线程泄露。**线程池应该是全局单例或 Spring Bean**。
2.  **吞没异常**：`submit()` 提交的任务如果抛出异常，不会直接打印到控制台，需要通过 `future.get()` 获取或在任务内 `try-catch`。
3.  **ThreadLocal 污染**：线程池线程是复用的，如果使用了 `ThreadLocal` 且没清理（`remove`），下个任务可能会读取到脏数据。
