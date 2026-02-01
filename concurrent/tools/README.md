# 并发工具学习与实战

- 面向协作与同步的工具：CountDownLatch、CyclicBarrier、Phaser、Exchanger。
- 面向限流与并发控制的工具：Semaphore。
- 面向通信与缓冲的工具：BlockingQueue、SynchronousQueue。
- 面向数据一致性的工具：ReadWriteLock、AtomicInteger。

## 基础概念
- CountDownLatch：倒计时门闩，等待一组任务完成后继续。
- CyclicBarrier：循环栅栏，一组线程到达后同时继续，可重复使用。
- Semaphore：信号量，限制同时访问资源的并发数。
- Phaser：分阶段同步，比栅栏更灵活，可动态注册/注销参与者。
- Exchanger：两个线程间数据交换。
- BlockingQueue：阻塞队列，适合生产者-消费者。
- SynchronousQueue：零缓冲队列，生产与消费一一配对。
- ReadWriteLock：读写锁，读并发、写独占。
- AtomicInteger：CAS 原子操作，轻量级计数器。

## 代码示例
- 倒计时等待所有任务完成：[CountDownLatchExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/tools/src/main/java/com/yineng/bpe/CountDownLatchExample.java)
- 循环栅栏统一起步与协作：[CyclicBarrierExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/tools/src/main/java/com/yineng/bpe/CyclicBarrierExample.java)
- 并发限流的典型实现：[SemaphoreExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/tools/src/main/java/com/yineng/bpe/SemaphoreExample.java)
- 多阶段任务协作：[PhaserExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/tools/src/main/java/com/yineng/bpe/PhaserExample.java)
- 双线程数据交接：[ExchangerExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/tools/src/main/java/com/yineng/bpe/ExchangerExample.java)
- 零缓冲配对传递：[SynchronousQueueExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/tools/src/main/java/com/yineng/bpe/SynchronousQueueExample.java)
- 生产者-消费者流水线：[BlockingQueuePipelineExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/tools/src/main/java/com/yineng/bpe/BlockingQueuePipelineExample.java)
- 读写分离提高并发读性能：[ReadWriteLockExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/tools/src/main/java/com/yineng/bpe/ReadWriteLockExample.java)
- 轻量级原子计数器：[AtomicIntegerExample.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/tools/src/main/java/com/yineng/bpe/AtomicIntegerExample.java)
- 统一入口运行示例：[Main.java](file:///Users/lixiaofeng/IdeaProjects/github/java-tutorials/concurrent/tools/src/main/java/com/yineng/bpe/Main.java)

## 常见场景
- 网关/接口限流：Semaphore 控制并发、避免过载。
- 批处理协作：CountDownLatch 汇总完成，CyclicBarrier/Phaser 阶段性协作。
- 数据管道：BlockingQueue 连接生产和消费，SynchronousQueue 实现严格配对。
- 缓存或配置读多写少：ReadWriteLock 提升读性能，写时独占。
- 高性能计数：AtomicInteger 用于指标、请求计数、自增 ID（非分布式）。
- 双通道交换：Exchanger 在配对线程间交付批次数据。

## 最容易踩的坑
- 栅栏破损：CyclicBarrier 某个线程异常导致 barrier broken，需要捕获并处理。
- 漏释放：Semaphore acquire 后未 release 导致长期阻塞。
- 死锁或饥饿：过多互斥、资源争用导致系统停滞；合理设置线程池与工具参数。
- 错误的中断处理：阻塞操作未正确响应中断，导致取消无效。
- 队列容量与背压：BlockingQueue 未合理设置 capacity，出现 OOM 或吞吐骤降。
- 读写锁升级：持读锁再请求写锁会导致死锁，应释放读锁后再获取写锁。
- 参与者数量变化：Phaser 需正确注册/注销，否则 await 永远无法通过。

## 选择建议
- 等待一组任务结束：CountDownLatch。
- 阶段性协作、可复用：CyclicBarrier 或 Phaser（更灵活）。
- 并发限流：Semaphore。
- 数据管道：BlockingQueue；严格配对：SynchronousQueue。
- 读多写少：ReadWriteLock；轻量计数：AtomicInteger。

## 运行方式
- IDE 运行：执行 Main.main。
- 命令行构建：需在本地安装 Maven；或告诉我标准构建命令以便记录并自动校验。
