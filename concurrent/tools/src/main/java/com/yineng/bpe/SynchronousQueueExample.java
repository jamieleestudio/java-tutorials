package com.yineng.bpe;

import java.util.concurrent.SynchronousQueue;

public class SynchronousQueueExample {
    public static void run() {
        SynchronousQueue<String> q = new SynchronousQueue<>();
        Thread producer = new Thread(() -> {
            try {
                System.out.println("SynchronousQueue put X");
                q.put("X");
                System.out.println("SynchronousQueue put Y");
                q.put("Y");
            } catch (Exception e) {
                System.out.println("SynchronousQueue producer error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
            }
        });
        Thread consumer = new Thread(() -> {
            try {
                String a = q.take();
                System.out.println("SynchronousQueue take " + a);
                String b = q.take();
                System.out.println("SynchronousQueue take " + b);
            } catch (Exception e) {
                System.out.println("SynchronousQueue consumer error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
            }
        });
        producer.start();
        consumer.start();
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            System.out.println("SynchronousQueue join error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }
}
