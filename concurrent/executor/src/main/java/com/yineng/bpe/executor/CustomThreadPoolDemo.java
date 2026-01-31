package com.yineng.bpe.executor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示生产环境推荐的 ThreadPoolExecutor 手动创建方式
 */
public class CustomThreadPoolDemo {

    public static void main(String[] args) {
        // 1. 自定义参数
        int corePoolSize = 2;
        int maxPoolSize = 4;
        long keepAliveTime = 10;
        int queueCapacity = 2;

        // 2. 自定义线程工厂（建议给线程起个有意义的名字，方便排查问题）
        ThreadFactory namedThreadFactory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "My-Biz-Thread-" + count.getAndIncrement());
            }
        };

        // 3. 自定义拒绝策略（这里演示 CallerRunsPolicy，由调用者线程执行，起到限流作用）
        RejectedExecutionHandler policy = new ThreadPoolExecutor.CallerRunsPolicy();
        // 也可以用 AbortPolicy (抛异常), DiscardPolicy (丢弃), DiscardOldestPolicy (丢弃最老)

        // 4. 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity), // 有界队列
                namedThreadFactory,
                policy
        );

        System.out.println("--- 提交任务开始 ---");
        
        // 模拟提交 10 个任务
        // core=2, max=4, queue=2. 
        // 前2个任务 -> core threads
        // 第3,4个任务 -> queue
        // 第5,6个任务 -> max threads (new threads)
        // 第7个任务 -> queue满且pool满 -> 触发拒绝策略 (CallerRunsPolicy -> main线程执行)
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " 处理任务 " + taskId);
                    TimeUnit.SECONDS.sleep(1); // 模拟耗时
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        
        System.out.println("--- 所有任务提交完毕 ---");
        
        executor.shutdown();
    }
}
