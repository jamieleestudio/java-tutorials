package com.yineng.bpe.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class FireAndForgetDemo {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private int n = 1;
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "fire-task-" + n++);
                t.setUncaughtExceptionHandler((th, ex) -> System.out.println("Uncaught " + th.getName() + " " + ex.getClass().getSimpleName() + ":" + ex.getMessage()));
                return t;
            }
        });
        CompletableFuture.runAsync(() -> {
            System.out.println("fire-1 start");
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("fire-1 interrupted");
                return;
            }
            System.out.println("fire-1 done");
        }, executor).exceptionally(ex -> {
            System.out.println("fire-1 error " + ex.getClass().getSimpleName() + ":" + ex.getMessage());
            return null;
        });
        CompletableFuture.runAsync(() -> {
            System.out.println("fire-2 start");
            throw new IllegalStateException("boom");
        }, executor).exceptionally(ex -> {
            System.out.println("fire-2 error " + ex.getClass().getSimpleName() + ":" + ex.getMessage());
            return null;
        });
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("fire-and-forget end");
    }
}
