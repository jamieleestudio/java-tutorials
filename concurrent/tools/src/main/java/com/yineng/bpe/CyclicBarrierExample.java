package com.yineng.bpe;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CyclicBarrierExample {
    public static void run() {
        int parties = 3;
        ExecutorService executor = Executors.newFixedThreadPool(parties);
        CyclicBarrier barrier = new CyclicBarrier(parties, () -> System.out.println("CyclicBarrier all arrived"));
        try {
            for (int i = 1; i <= parties; i++) {
                final int id = i;
                executor.submit(() -> {
                    try {
                        Thread.sleep(150L * id);
                        System.out.println("CyclicBarrier worker-" + id + " awaiting");
                        barrier.await();
                        System.out.println("CyclicBarrier worker-" + id + " passed");
                    } catch (Exception e) {
                        System.out.println("CyclicBarrier worker-" + id + " error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("CyclicBarrier error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
