package com.yineng.bpe.distributedlock.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;

/**
 * 基于行级排他锁的悲观锁实现。
 * 利用 SELECT ... FOR UPDATE 对某一行加行锁，事务提交或回滚后自动释放。
 * 需要张 lock_table 表，lock_name 列为主键。
 */
public class MysqlPessimisticLock implements Lock {

    private final Connection connection;
    private final String lockName;

    public MysqlPessimisticLock(Connection connection, String lockName) {
        this.connection = connection;
        this.lockName = lockName;
    }

    @Override
    public void lock() {
        while (!trySelectForUpdate()) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        while (!trySelectForUpdate()) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
        }
    }

    @Override
    public boolean tryLock() {
        return trySelectForUpdate();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(time);
        while (!trySelectForUpdate()) {
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
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("unlock(commit) failed: " + lockName, e);
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("MysqlPessimisticLock does not support Condition");
    }

    private boolean trySelectForUpdate() {
        String sql = "SELECT lock_name FROM lock_table WHERE lock_name = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, lockName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
            connection.rollback();
            return false;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            return false;
        }
    }
}