package com.yineng.bpe.distributedlock.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;

/**
 * 基于 Jedis SET ... NX PX 的分布式锁实现。
 * 加锁：SET key value NX PX <ttl>，value 为唯一标识用于安全释放。
 * 释放：Lua 脚本保证「判断 value + 删除」原子性，防止误删别人的锁。
 */
public class JedisDistributedLock implements Lock {

    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    private final Jedis jedis;
    private final String lockKey;
    private final String lockValue;
    private final long leaseMillis;

    public JedisDistributedLock(Jedis jedis, String lockKey, long leaseMillis) {
        this.jedis = jedis;
        this.lockKey = lockKey;
        this.lockValue = UUID.randomUUID().toString();
        this.leaseMillis = leaseMillis;
    }

    @Override
    public void lock() {
        while (!trySet()) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        while (!trySet()) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
        }
    }

    @Override
    public boolean tryLock() {
        return trySet();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(time);
        while (!trySet()) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (System.nanoTime() >= deadlineNanos) {
                return false;
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
        }
        return true;
    }

    @Override
    public void unlock() {
        Object result = jedis.eval(UNLOCK_SCRIPT, 1, lockKey, lockValue);
        if (result == null || ((Long) result) == 0L) {
            throw new IllegalStateException("unlock failed, lock already released or expired: " + lockKey);
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("JedisDistributedLock does not support Condition");
    }

    private boolean trySet() {
        SetParams params = SetParams.setParams().nx().px(leaseMillis);
        String result = jedis.set(lockKey, lockValue, params);
        return "OK".equals(result);
    }
}