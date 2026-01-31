package com.yineng.bpe.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 演示 Executors 工厂类的基本用法（仅供学习，生产环境建议手动创建 ThreadPoolExecutor）
 */
public class BasicPoolDemo {

    public static void main(String[] args) {
        // 1. 固定大小的线程池 (FixedThreadPool)
        // 适用于负载较重的服务器，限制并发数
        testFixedThreadPool();

        // 2. 单线程池 (SingleThreadExecutor)
        // 适用于需要保证顺序执行的场景
        testSingleThreadExecutor();

        // 3. 可缓存线程池 (CachedThreadPool)
        // 适用于执行很多短期异步任务的小程序，负载较轻的服务器
        testCachedThreadPool();
    }

    private static void testFixedThreadPool() {
        System.out.println("--- FixedThreadPool ---");
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " is processing task " + taskId);
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        
        shutdownAndAwait(executor);
    }

    private static void testSingleThreadExecutor() {
        System.out.println("\n--- SingleThreadExecutor ---");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        for (int i = 0; i < 3; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " is processing task " + taskId);
            });
        }
        shutdownAndAwait(executor);
    }

    private static void testCachedThreadPool() {
        System.out.println("\n--- CachedThreadPool ---");
        ExecutorService executor = Executors.newCachedThreadPool();
        
        // 提交很多短任务
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " is processing task " + taskId);
            });
        }
        shutdownAndAwait(executor);
    }

    private static void shutdownAndAwait(ExecutorService pool) {
        pool.shutdown(); // 不再接收新任务
        try {
            // 等待现有任务完成
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // 超时强制关闭
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
    }
}
