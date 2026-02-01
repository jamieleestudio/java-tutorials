package com.yineng.bpe;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingQueuePipelineExample {
    public static void run() {
        BlockingQueue<Integer> q = new LinkedBlockingQueue<>(5);
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    q.put(i);
                    System.out.println("BlockingQueue produced " + i);
                }
                q.put(-1);
            } catch (Exception e) {
                System.out.println("BlockingQueue producer error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
            }
        });
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    Integer v = q.take();
                    if (v == -1) break;
                    System.out.println("BlockingQueue consumed " + v);
                }
            } catch (Exception e) {
                System.out.println("BlockingQueue consumer error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
            }
        });
        producer.start();
        consumer.start();
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            System.out.println("BlockingQueue join error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }
}
