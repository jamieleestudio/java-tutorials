package com.yineng.bpe.future;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 演示 Java Future 的基本用法
 */
public class BasicFutureDemo {

    public static void main(String[] args) {
        // 1. 创建一个线程池
        ExecutorService executor = Executors.newSingleThreadExecutor();
        System.out.println("Main: 提交异步任务...");

        // 2. 提交任务，获得 Future 对象
        Future<String> future = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println("Task: 开始耗时计算...");
                Thread.sleep(2000); // 模拟耗时操作
                System.out.println("Task: 计算完成！");
                return "Hello, Future!";
            }
        });

        System.out.println("Main: 可以在这里做其他事情...");
        try {
            Thread.sleep(500); // 模拟主线程做其他事
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. 检查任务是否完成
        if (!future.isDone()) {
            System.out.println("Main: 任务还没好，我再等会儿...");
        }

        try {
            // 4. 获取结果
            // get() 是阻塞的，直到任务完成
            // String result = future.get(); 
            
            // 建议使用带超时的 get，防止无限等待
            String result = future.get(3, TimeUnit.SECONDS);
            System.out.println("Main: 拿到结果了 -> " + result);

        } catch (InterruptedException e) {
            // 当前线程被中断
            e.printStackTrace();
        } catch (ExecutionException e) {
            // 任务执行过程中抛出了异常
            System.out.println("Main: 任务执行出错 -> " + e.getCause().getMessage());
        } catch (TimeoutException e) {
            // 超时
            System.out.println("Main: 等太久了，不等了！");
            future.cancel(true); // 可以选择取消任务
        } finally {
            // 5. 关闭线程池
            executor.shutdown();
        }
    }
}
