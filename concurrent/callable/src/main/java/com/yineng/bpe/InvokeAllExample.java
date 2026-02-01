package com.yineng.bpe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InvokeAllExample {
    public static void run() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                final int id = i;
                tasks.add(() -> {
                    Thread.sleep(100L * id);
                    return "task-" + id;
                });
            }
            List<Future<String>> futures = executor.invokeAll(tasks);
            for (Future<String> f : futures) {
                System.out.println("InvokeAllExample result=" + f.get());
            }
        } catch (Exception e) {
            System.out.println("InvokeAllExample error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
