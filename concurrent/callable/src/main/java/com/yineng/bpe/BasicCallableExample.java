package com.yineng.bpe;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class BasicCallableExample {
    public static void run() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Callable<Integer> sumTask = () -> IntStream.rangeClosed(1, 10).sum();
            Future<Integer> future = executor.submit(sumTask);
            Integer result = future.get();
            System.out.println("BasicCallableExample result=" + result);
        } catch (Exception e) {
            System.out.println("BasicCallableExample error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
