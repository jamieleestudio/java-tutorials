package com.yineng.bpe;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CountDownLatchExample {
    public static void run() {
        int workers = 3;
        CountDownLatch ready = new CountDownLatch(workers);
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        try {
            for (int i = 1; i <= workers; i++) {
                final int id = i;
                executor.submit(() -> {
                    try {
                        Thread.sleep(200L * id);
                        System.out.println("CountDownLatch worker-" + id + " finished");
                    } catch (Exception e) {
                        System.out.println("CountDownLatch worker-" + id + " error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
                    } finally {
                        ready.countDown();
                    }
                });
            }
            ready.await();
            System.out.println("CountDownLatch all workers done");
        } catch (Exception e) {
            System.out.println("CountDownLatch error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
