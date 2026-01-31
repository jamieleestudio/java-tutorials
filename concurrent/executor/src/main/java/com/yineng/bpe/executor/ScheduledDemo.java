package com.yineng.bpe.executor;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 演示 ScheduledExecutorService 的定时任务功能
 */
public class ScheduledDemo {

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        System.out.println("Start time: " + new Date());

        // 1. 延迟执行 (只执行一次)
        scheduler.schedule(() -> {
            System.out.println("Delayed task executed at: " + new Date());
        }, 2, TimeUnit.SECONDS);

        // 2. 固定频率执行 (AtFixedRate)
        // 第一次延迟1秒执行，之后每隔3秒执行一次。
        // 注意：如果任务耗时超过周期（例如耗时5秒，周期3秒），则上一个任务做完，下一个立刻开始（不会并发执行，但会推迟）。
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("FixedRate task executed at: " + new Date());
            try {
                // 模拟耗时 1 秒
                TimeUnit.SECONDS.sleep(1); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 1, 3, TimeUnit.SECONDS);

        // 3. 固定延迟执行 (WithFixedDelay)
        // 第一次延迟1秒执行。
        // 之后每次任务**结束**后，再等待3秒，才执行下一个。
        // 所以时间间隔 = 任务耗时 + delay
        scheduler.scheduleWithFixedDelay(() -> {
            System.out.println("FixedDelay task executed at: " + new Date());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 1, 3, TimeUnit.SECONDS);

        // 让主线程运行一段时间，观察定时任务的输出
        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Shutting down...");
        scheduler.shutdown();
    }
}
