package com.yineng.bpe.volatiles.examples;

import java.util.concurrent.CountDownLatch;

public class VolatileDclSingletonDemo {

    public static void main(String[] args) throws Exception {
        int threads = 16;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    Singleton s = Singleton.getInstance();
                    if (s.value != 42) {
                        System.out.println("unexpected value: " + s.value);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        done.await();
        System.out.println("done");
    }

    private static final class Singleton {
        private static volatile Singleton instance;
        private final int value;

        private Singleton() {
            this.value = 42;
        }

        static Singleton getInstance() {
            Singleton local = instance;
            if (local == null) {
                synchronized (Singleton.class) {
                    local = instance;
                    if (local == null) {
                        local = new Singleton();
                        instance = local;
                    }
                }
            }
            return local;
        }
    }
}

