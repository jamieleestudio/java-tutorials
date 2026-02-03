package com.yineng.bpe.volatiles.examples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class VolatileReferencePublicationDemo {

    private static volatile Config config = new Config("v1", 1);

    public static void main(String[] args) throws Exception {
        int readers = 4;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(readers);

        for (int i = 0; i < readers; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
                    int observed = 0;
                    while (System.nanoTime() < end) {
                        Config c = config;
                        if (!c.name.equals("v1") && !c.name.equals("v2")) {
                            throw new IllegalStateException("unexpected name: " + c.name);
                        }
                        if (c.version != 1 && c.version != 2) {
                            throw new IllegalStateException("unexpected version: " + c.version);
                        }
                        observed++;
                    }
                    System.out.println("reader observed " + observed + " snapshots");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        TimeUnit.MILLISECONDS.sleep(100);
        config = new Config("v2", 2);

        done.await();
        System.out.println("final config = " + config.name + ":" + config.version);
    }

    private static final class Config {
        private final String name;
        private final int version;

        private Config(String name, int version) {
            this.name = name;
            this.version = version;
        }
    }
}

