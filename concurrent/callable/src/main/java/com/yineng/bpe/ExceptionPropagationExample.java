package com.yineng.bpe;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExceptionPropagationExample {
    public static void run() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> future = executor.submit(() -> {
                if (true) throw new IllegalStateException("boom");
                return 1;
            });
            try {
                future.get();
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                System.out.println("ExceptionPropagationExample cause=" + cause.getClass().getSimpleName() + ":" + cause.getMessage());
            }
        } catch (Exception e) {
            System.out.println("ExceptionPropagationExample error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
