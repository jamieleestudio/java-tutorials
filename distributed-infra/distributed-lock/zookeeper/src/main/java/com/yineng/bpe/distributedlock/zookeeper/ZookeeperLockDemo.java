package com.yineng.bpe.distributedlock.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ZooKeeper 分布式锁演示。
 * 使用 Curator InterProcessMutex（可重入公平锁）。
 * 连接 localhost:2181，需要先启动 ZooKeeper 服务。
 */
@Slf4j
public class ZookeeperLockDemo {

    private static final String ZK_CONNECT_STRING = "localhost:2181";
    private static final String LOCK_PATH = "/locks/resource_A";

    public static void main(String[] args) throws Exception {
        if (!isZkAvailable()) {
            log.warn("ZooKeeper not available at {}, skip demo", ZK_CONNECT_STRING);
            return;
        }

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(ZK_CONNECT_STRING)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();

        AtomicInteger counter = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                InterProcessMutex mutex = new InterProcessMutex(client, LOCK_PATH);
                ZookeeperDistributedLock lock = new ZookeeperDistributedLock(mutex);
                try {
                    if (lock.tryLock(5, TimeUnit.SECONDS)) {
                        try {
                            counter.incrementAndGet();
                            log.info("{} acquired lock", Thread.currentThread().getName());
                        } finally {
                            lock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("zookeeper lock demo finished, counter={}", counter.get());

        client.close();
    }

    private static boolean isZkAvailable() {
        try (CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(ZK_CONNECT_STRING)
                .sessionTimeoutMs(3000)
                .connectionTimeoutMs(3000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 1))
                .build()) {
            client.start();
            client.blockUntilConnected(3, TimeUnit.SECONDS);
            return client.getZookeeperClient().isConnected();
        } catch (Exception e) {
            return false;
        }
    }
}