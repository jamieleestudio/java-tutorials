package com.yineng.bpe.distributedlock.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis 分布式锁演示。
 * 包含两种客户端实现：
 *   1. Jedis SETNX 手写实现 JedisDistributedLock
 *   2. Redisson RLock 框架实现（支持可重入、看门狗自动续期）
 * 连接 localhost:6379，需要先启动 Redis。
 */
@Slf4j
public class RedisLockDemo {

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;

    public static void main(String[] args) throws Exception {
        if (!isRedisAvailable()) {
            log.warn("Redis not available at {}:{}, skip demo", REDIS_HOST, REDIS_PORT);
            return;
        }
        jedisLockDemo();
        redissonLockDemo();
    }

    private static void jedisLockDemo() {
        log.info("=== JedisDistributedLock demo ===");
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
            AtomicInteger counter = new AtomicInteger(0);
            ExecutorService executor = Executors.newFixedThreadPool(5);
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> {
                    JedisDistributedLock lock = new JedisDistributedLock(jedis, "lock:jedis:resource", 10000);
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
            log.info("jedis lock demo finished, counter={}", counter.get());
        }
    }

    private static void redissonLockDemo() {
        log.info("=== Redisson RLock demo ===");
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + REDIS_HOST + ":" + REDIS_PORT);
        RedissonClient redisson = Redisson.create(config);

        AtomicInteger counter = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                RLock lock = redisson.getLock("lock:redisson:resource");
                try {
                    if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                        try {
                            counter.incrementAndGet();
                            log.info("{} acquired lock, isHeldByCurrentThread={}",
                                    Thread.currentThread().getName(), lock.isHeldByCurrentThread());
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
        log.info("redisson lock demo finished, counter={}", counter.get());
        redisson.shutdown();
    }

    private static boolean isRedisAvailable() {
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
            jedis.ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}