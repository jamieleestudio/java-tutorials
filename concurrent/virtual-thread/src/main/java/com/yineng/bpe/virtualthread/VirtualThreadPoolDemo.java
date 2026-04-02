package com.yineng.bpe.virtualthread;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * 虚拟线程池示例
 * 使用 Executors.newVirtualThreadPerTaskExecutor() 来处理并发任务。
 * 虚拟线程的特点是按需创建，不池化（No Pooling），因为其创建和销毁的开销极低。
 */
public class VirtualThreadPoolDemo {

    public static void main(String[] args) {
        System.out.println("=== 虚拟线程执行器示例 ===");

        // 使用 try-with-resources 语法，自动等待所有任务完成并关闭 Executor
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            
            // 提交 10 个任务到虚拟线程
            IntStream.range(0, 10).forEach(i -> {
                executor.submit(() -> {
                    // 模拟 IO 阻塞
                    try {
                        Thread.sleep(Duration.ofMillis(500));
                        System.out.println("任务 " + i + " 执行完毕 - " + Thread.currentThread());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });

            System.out.println("主线程提交完所有任务，等待虚拟线程执行...");
        } // 这里隐式调用了 executor.close()，会阻塞直到所有提交的任务完成

        System.out.println("所有虚拟线程任务执行完毕，Executor 已关闭。");
    }
}
