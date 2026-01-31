package com.yineng.bpe.future;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 演示 Future 使用中的常见坑
 */
public class FuturePitfallDemo {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 坑点1：滥用 get() 导致串行化
        // blockingGetDemo(executor);

        // 坑点2：cancel 无法停止“不响应中断”的任务
        cancelDemo(executor);

        executor.shutdown();
    }

    private static void blockingGetDemo(ExecutorService executor) {
        System.out.println("--- Pitfall 1: Blocking Get ---");
        long start = System.currentTimeMillis();

        // 错误示范：提交一个任务立刻 get，导致无法并行
        // 假设我们有两个耗时任务
        try {
            Future<String> f1 = executor.submit(() -> {
                TimeUnit.SECONDS.sleep(1);
                return "Task 1";
            });
            // 这里的 get 会阻塞，直到 f1 完成，才去提交 f2
            // 实际上并没有并行！
            System.out.println(f1.get()); 

            Future<String> f2 = executor.submit(() -> {
                TimeUnit.SECONDS.sleep(1);
                return "Task 2";
            });
            System.out.println(f2.get());

            System.out.println("Total time (Wrong way): " + (System.currentTimeMillis() - start) + "ms (Expected ~2000ms)");

            // 正确示范：先全部提交，最后再 get
            start = System.currentTimeMillis();
            Future<String> f3 = executor.submit(() -> {
                TimeUnit.SECONDS.sleep(1);
                return "Task 3";
            });
            Future<String> f4 = executor.submit(() -> {
                TimeUnit.SECONDS.sleep(1);
                return "Task 4";
            });
            
            System.out.println(f3.get());
            System.out.println(f4.get());
            System.out.println("Total time (Right way): " + (System.currentTimeMillis() - start) + "ms (Expected ~1000ms)");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void cancelDemo(ExecutorService executor) {
        System.out.println("\n--- Pitfall 2: Cancel not working ---");
        
        // 提交一个死循环任务，且不检查中断状态
        Future<?> future = executor.submit(() -> {
            System.out.println("Task started... (Uninterruptible)");
            // 模拟一个忽略中断的耗时操作
            long i = 0;
            while (i < Long.MAX_VALUE) {
                i++;
                // 必须加上这行检查，否则 cancel(true) 也没用
                 if (Thread.currentThread().isInterrupted()) {
                     System.out.println("Task detected interruption! Exiting...");
                     break;
                 }
            }
            System.out.println("Task finished (or stopped).");
        });

        try {
            TimeUnit.MILLISECONDS.sleep(100);
            System.out.println("Main: Trying to cancel task...");
            boolean cancelled = future.cancel(true); // 发送中断信号
            System.out.println("Main: Cancel success? " + cancelled);
            
            // 等待一会儿看看任务停了没（观察控制台输出）
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
