package com.yineng.bpe.distributedlock.mysql;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MySQL 分布式锁演示。
 * 使用 H2 内存数据库模拟（H2 兼容 MySQL 语法，支持 FOR UPDATE 行和唯一索引）。
 * 包含两种实现：
 *   1. 唯一索引排他锁 MysqlUniqueIndexLock —— 靠唯一索引冲突保证互斥
 *   2. 悲观行锁 MysqlPessimisticLock      —— 靠 SELECT ... FOR UPDATE 加行锁
 */
@Slf4j
public class MysqlLockDemo {

    private static final String JDBC_URL = "jdbc:h2:mem:lock_demo;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static void main(String[] args) throws Exception {
        initTable();
        uniqueIndexLockDemo();
        pessimisticLockDemo();
    }

    private static void initTable() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS lock_table");
            stmt.execute("CREATE TABLE lock_table(lock_name VARCHAR(255) PRIMARY KEY, owner_id VARCHAR(255))");
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_lock_name ON lock_table(lock_name)");
        }
        log.info("lock_table initialized");
    }

    private static void uniqueIndexLockDemo() throws SQLException {
        log.info("=== MysqlUniqueIndexLock demo ===");
        AtomicInteger counter = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try (Connection conn = getConnection()) {
                    conn.setAutoCommit(false);
                    MysqlUniqueIndexLock lock = new MysqlUniqueIndexLock(conn, "resource_A");
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
                } catch (SQLException e) {
                    log.error("connection error", e);
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("unique index lock demo finished, counter={}", counter.get());
    }

    private static void pessimisticLockDemo() throws SQLException {
        log.info("=== MysqlPessimisticLock demo ===");
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM lock_table WHERE lock_name = 'resource_B'");
            stmt.execute("INSERT INTO lock_table(lock_name, owner_id) VALUES('resource_B', 'init')");
        }
        AtomicInteger counter = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try (Connection conn = getConnection()) {
                    conn.setAutoCommit(false);
                    MysqlPessimisticLock lock = new MysqlPessimisticLock(conn, "resource_B");
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
                } catch (SQLException e) {
                    log.error("connection error", e);
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("pessimistic lock demo finished, counter={}", counter.get());
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }
}