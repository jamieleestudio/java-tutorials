package com.yineng.bpe.distributedlock.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;

/**
 * 基于唯一索引的排他锁实现。
 * 利用 MySQL 唯一索引约束实现加锁，删除记录实现释放锁。
 * 需要一张 lock_table 表，lock_name 列建有唯一索引。
 */
public class MysqlUniqueIndexLock implements Lock {

    private final Connection connection;
    private final String lockName;
    private final String ownerId;

    public MysqlUniqueIndexLock(Connection connection, String lockName) {
        this.connection = connection;
        this.lockName = lockName;
        this.ownerId = UUID.randomUUID().toString();
    }

    @Override
    public void lock() {
        while (!tryInsert()) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        while (!tryInsert()) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
        }
    }

    @Override
    public boolean tryLock() {
        return tryInsert();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(time);
        while (!tryInsert()) {
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
        String sql = "DELETE FROM lock_table WHERE lock_name = ? AND owner_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, lockName);
            ps.setString(2, ownerId);
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("unlock failed: " + lockName, e);
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("MysqlUniqueIndexLock does not support Condition");
    }

    private boolean tryInsert() {
        String sql = "INSERT INTO lock_table(lock_name, owner_id) VALUES(?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, lockName);
            ps.setString(2, ownerId);
            ps.executeUpdate();
            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            return false;
        }
    }
}