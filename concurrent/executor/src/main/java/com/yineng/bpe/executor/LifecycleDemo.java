package com.yineng.bpe.executor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 演示线程池的优雅关闭 (Graceful Shutdown)
 */
public class LifecycleDemo {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 提交几个耗时任务
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Task " + taskId + " started.");
                    TimeUnit.SECONDS.sleep(2); // 模拟任务执行
                    System.out.println("Task " + taskId + " finished.");
                } catch (InterruptedException e) {
                    System.out.println("Task " + taskId + " was interrupted.");
                }
            });
        }

        System.out.println("Main: Initiating shutdown...");
        // 1. shutdown(): 停止接收新任务，但已提交的任务（包括在队列中的）会继续执行
        executor.shutdown();

        try {
            // 2. awaitTermination: 等待一段时间，让现有任务执行完
            System.out.println("Main: Waiting for tasks to complete...");
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                // 如果超时还没执行完
                System.err.println("Main: Timeout! Forcing shutdown...");
                
                // 3. shutdownNow(): 尝试中断正在执行的任务，并返回等待队列中未执行的任务
                List<Runnable> droppedTasks = executor.shutdownNow();
                System.err.println("Main: Dropped " + droppedTasks.size() + " tasks.");
            } else {
                System.out.println("Main: All tasks completed successfully.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            executor.shutdownNow();
        }
    }
}
