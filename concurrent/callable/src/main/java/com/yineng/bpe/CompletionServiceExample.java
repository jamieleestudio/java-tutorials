package com.yineng.bpe;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CompletionServiceExample {
    public static void run() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CompletionService<String> cs = new ExecutorCompletionService<>(executor);
        try {
            for (int i = 1; i <= 5; i++) {
                final int id = i;
                cs.submit(task(id));
            }
            for (int i = 0; i < 5; i++) {
                Future<String> f = cs.take();
                System.out.println("CompletionServiceExample result=" + f.get());
            }
        } catch (Exception e) {
            System.out.println("CompletionServiceExample error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private static Callable<String> task(int id) {
        return () -> {
            Thread.sleep(120L * (6 - id));
            return "done-" + id;
        };
    }
}
