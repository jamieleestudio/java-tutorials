package com.yineng.bpe.distributedlock.zookeeper;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;

/**
 * 基于 ZooKeeper InterProcessMutex 的分布式锁实现。
 * InterProcessMutex 是可重入锁，基于临时顺序节点 + Watch 机制实现公平锁。
 * 释放时需要调用 release()，否会残留临时节点（会话断开时自动清理）。
 */
public class ZookeeperDistributedLock implements Lock {

    private final InterProcessMutex mutex;

    public ZookeeperDistributedLock(InterProcessMutex mutex) {
        this.mutex = mutex;
    }

    @Override
    public void lock() {
        try {
            mutex.acquire();
        } catch (Exception e) {
            throw new RuntimeException("lock failed", e);
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        try {
            if (!mutex.acquire(0, TimeUnit.MILLISECONDS)) {
                while (!tryAcquireNoWait()) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
                }
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("lockInterruptibly failed", e);
        }
    }

    @Override
    public boolean tryLock() {
        return tryAcquireNoWait();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            return mutex.acquire(time, unit);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("tryLock failed", e);
        }
    }

    @Override
    public void unlock() {
        try {
            mutex.release();
        } catch (Exception e) {
            throw new RuntimeException("unlock failed", e);
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("ZookeeperDistributedLock does not support Condition");
    }

    private boolean tryAcquireNoWait() {
        try {
            return mutex.acquire(0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            throw new RuntimeException("tryAcquireNoWait failed", e);
        }
    }
}