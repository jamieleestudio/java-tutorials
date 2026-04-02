package com.yineng.bpe.virtualthread;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 虚拟线程性能对比示例
 * 演示创建 10,000 个模拟 IO 密集型阻塞的任务，分别使用传统线程池和虚拟线程
 * 在大规模并发和高阻塞场景下，虚拟线程优势明显。
 */
public class PerformanceComparisonDemo {

    // 假设有 10,000 个任务需要执行
    private static final int TASK_COUNT = 10_000;

    public static void main(String[] args) {
        System.out.println("=== 性能对比: 平台线程 vs 虚拟线程 ===");
        System.out.println("任务数量: " + TASK_COUNT);
        
        // 1. 测试平台线程 (使用固定大小的线程池)
        // 注意：如果直接 new 10,000 个普通 Thread，可能会导致 OutOfMemoryError 或操作系统崩溃
        // 因此使用固定线程池，但这会导致总耗时大大增加，因为任务需要排队
        long platformTime = testPerformance("平台线程 (FixedThreadPool: 100)", 
                                            Executors.newFixedThreadPool(100));

        // 2. 测试虚拟线程
        long virtualTime = testPerformance("虚拟线程 (VirtualThreadPerTaskExecutor)", 
                                           Executors.newVirtualThreadPerTaskExecutor());

        System.out.println("\n--- 测试总结 ---");
        System.out.println("平台线程(100线程) 耗时: " + platformTime + " ms");
        System.out.println("虚拟线程(不池化)  耗时: " + virtualTime + " ms");
    }

    private static long testPerformance(String testName, ExecutorService executor) {
        System.out.println("\n开始测试: " + testName);
        AtomicInteger counter = new AtomicInteger();
        Instant start = Instant.now();

        try (executor) { // try-with-resources 自动管理关闭并等待完成
            IntStream.range(0, TASK_COUNT).forEach(i -> {
                executor.submit(() -> {
                    try {
                        // 模拟 IO 阻塞 1 秒 (例如数据库查询或 HTTP 请求)
                        Thread.sleep(Duration.ofSeconds(1));
                        counter.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
        } // 阻塞直到所有任务结束

        Instant end = Instant.now();
        long timeElapsed = Duration.between(start, end).toMillis();
        System.out.println("完成任务数: " + counter.get() + ", 耗时: " + timeElapsed + " ms");
        return timeElapsed;
    }
}
