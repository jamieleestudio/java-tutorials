package com.yineng.bpe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockExample {
    public static void run() {
        ReadWriteLock rw = new ReentrantReadWriteLock();
        Map<String, Integer> store = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            executor.submit(() -> {
                rw.writeLock().lock();
                try {
                    store.put("x", 1);
                    System.out.println("ReadWriteLock write x=1");
                } finally {
                    rw.writeLock().unlock();
                }
            });
            for (int i = 1; i <= 3; i++) {
                executor.submit(() -> {
                    rw.readLock().lock();
                    try {
                        Integer v = store.get("x");
                        System.out.println("ReadWriteLock read x=" + v);
                    } finally {
                        rw.readLock().unlock();
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("ReadWriteLock error=" + e.getClass().getSimpleName() + ":" + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
