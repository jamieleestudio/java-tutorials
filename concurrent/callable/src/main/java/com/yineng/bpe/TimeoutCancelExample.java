package com.yineng.bpe;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeoutCancelExample {
    public static void run() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> future = executor.submit(() -> {
                Thread.sleep(2000);
                return "slow";
            });
            try {
                String r = future.get(300, TimeUnit.MILLISECONDS);
                System.out.println("TimeoutCancelExample result=" + r);
            } catch (TimeoutException e) {
                boolean cancelled = future.cancel(true);
                System.out.println("TimeoutCancelExample timeout cancelled=" + cancelled);
            }
        } catch (Exception e) {
            System.out.println("TimeoutCancelExample error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }
}
