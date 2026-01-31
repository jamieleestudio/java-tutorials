package com.yineng.bpe.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 演示 CompletableFuture 的强大功能
 */
public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 简单的异步执行
        simpleAsync();

        // 2. 任务链式调用 (thenApply, thenAccept)
        chaining();

        // 3. 组合多个任务 (allOf)
        combine();

        // 4. 已知结果直接返回 (completedFuture)
        completedFutureExample();
        
        // 保持主线程不退出，以便观察异步结果（因为CompletableFuture默认使用守护线程池）
        Thread.sleep(3000);
    }

    private static void simpleAsync() throws ExecutionException, InterruptedException {
        System.out.println("--- 1. Simple Async ---");
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello";
        });

        // 可以在这里做回调，也可以直接 get
        System.out.println("Result: " + future.get());
    }

    private static void chaining() {
        System.out.println("--- 2. Chaining ---");
        CompletableFuture.supplyAsync(() -> {
            // 步骤1：获取订单
            return "Order_123";
        }).thenApply(orderId -> {
            // 步骤2：根据订单获取支付金额 (依赖上一步结果)
            return orderId + " -> Amount: $100";
        }).thenApply(orderInfo -> {

            // 步骤3：添加发票信息
            return orderInfo + " -> Invoice: Created";
        }).thenAccept(finalResult -> {
            // 步骤4：消费最终结果
            System.out.println("Final Result: " + finalResult);
        }).exceptionally(ex -> {
            System.err.println("Oops! Something went wrong: " + ex.getMessage());
            return null;
        });
    }

    private static void combine() {
        System.out.println("--- 3. Combine ---");
        
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            sleepRandom();
            System.out.println("Task 1 done");
            return "Result 1";
        });

        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
            sleepRandom();
            System.out.println("Task 2 done");
            return "Result 2";
        });

        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
            sleepRandom();
            System.out.println("Task 3 done");
            return "Result 3";
        });

        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(task1, task2, task3);

        allFutures.thenRun(() -> {
            System.out.println("All tasks finished!");
            try {
                // 这里的 get 不会阻塞，因为已经确定都完成了
                System.out.println(task1.get() + ", " + task2.get() + ", " + task3.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void completedFutureExample() {
        System.out.println("--- 4. Completed Future ---");
        // 场景：如果数据已经准备好了（比如来自缓存、降级处理、单元测试mock），直接返回一个已经完成的 Future
        // 这样做的好处是统一了接口返回值（都是 Future），但避免了不必要的线程切换开销。
        // 
        // 【Spring @Async 常用模式】
        // 在 Spring 的 @Async 方法中，如果需要返回值，方法签名必须是 Future<T>。
        // 也就是在异步线程计算完成后，使用 CompletableFuture.completedFuture(result) 包装结果返回。
        // Spring 的 AOP 代理会自动处理异步桥接，将这个结果传递给主线程持有的 Future。
        
        String cachedValue = "Cached Value (Immediate)";
        CompletableFuture<String> future = CompletableFuture.completedFuture(cachedValue);

        // 验证它是否已经完成
        System.out.println("Is done? " + future.isDone());
        
        // 获取结果（不会阻塞，因为已经完成了）
        // join() 和 get() 类似，但只抛出 unchecked exception
        System.out.println("Result: " + future.join()); 
        
        // 即使是 completedFuture，依然支持链式调用
        future.thenAccept(val -> System.out.println("Consumed: " + val));
    }

    private static void sleepRandom() {
        try {
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(500, 2000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
