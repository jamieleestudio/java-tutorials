package com.yineng.bpe;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerExample {
    public static void run() {
        AtomicInteger counter = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> {
                    int v = counter.incrementAndGet();
                    System.out.println("AtomicInteger value=" + v);
                });
            }
        } catch (Exception e) {
            System.out.println("AtomicInteger error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
