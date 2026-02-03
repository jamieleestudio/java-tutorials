package com.yineng.bpe.volatiles.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class VolatileNotAtomicDemo {

    private static final int THREADS = 8;
    private static final int INCREMENTS_PER_THREAD = 200_000;

    private static volatile int counter = 0;

    public static void main(String[] args) throws Exception {
        counter = 0;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);

        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Thread t = new Thread(() -> {
                try {
                    start.await();
                    for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                        counter++;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
            workers.add(t);
            t.start();
        }

        long expected = (long) THREADS * INCREMENTS_PER_THREAD;
        start.countDown();
        done.await();

        System.out.println("expected = " + expected);
        System.out.println("actual   = " + counter);
        System.out.println("lost     = " + (expected - counter));
    }
}

