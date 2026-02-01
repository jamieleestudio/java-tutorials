package com.yineng.bpe;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class PhaserExample {
    public static void run() {
        Phaser phaser = new Phaser(3);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            for (int i = 1; i <= 3; i++) {
                final int id = i;
                executor.submit(() -> {
                    try {
                        System.out.println("Phaser worker-" + id + " phase-0 arrive");
                        phaser.arriveAndAwaitAdvance();
                        Thread.sleep(100L * id);
                        System.out.println("Phaser worker-" + id + " phase-1 arrive");
                        phaser.arriveAndAwaitAdvance();
                        System.out.println("Phaser worker-" + id + " done");
                    } catch (Exception e) {
                        System.out.println("Phaser worker-" + id + " error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Phaser error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
