package com.yineng.bpe;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class SemaphoreExample {
    public static void run() {
        int permits = 2;
        Semaphore semaphore = new Semaphore(permits);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            for (int i = 1; i <= 6; i++) {
                final int id = i;
                executor.submit(() -> {
                    try {
                        semaphore.acquire();
                        System.out.println("Semaphore worker-" + id + " acquired");
                        Thread.sleep(300);
                    } catch (Exception e) {
                        System.out.println("Semaphore worker-" + id + " error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
                    } finally {
                        semaphore.release();
                        System.out.println("Semaphore worker-" + id + " released");
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Semaphore error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
