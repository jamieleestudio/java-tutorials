package com.yineng.bpe.future;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConcurrentDbQueryDemo {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3, new ThreadFactory() {
            private int n = 1;
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "db-query-" + n++);
                t.setDaemon(true);
                return t;
            }
        });
        List<String> sqls = List.of(
                "SELECT id,name FROM user WHERE id=1",
                "SELECT count(*) FROM order WHERE status='PAID'",
                "SELECT * FROM product WHERE id=42"
        );
        List<CompletableFuture<Result>> futures = new ArrayList<>();
        for (int i = 0; i < sqls.size(); i++) {
            String sql = sqls.get(i);
            long latency = 300 + i * 200;
            boolean fail = i == 1;
            futures.add(CompletableFuture.supplyAsync(() -> query(sql, latency, fail), executor)
                    .orTimeout(2, TimeUnit.SECONDS)
                    .handle((r, ex) -> ex == null ? r : Result.failed(sql, ex)));
        }
        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        all.join();
        List<Result> results = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        long success = results.stream().filter(r -> r.success).count();
        long failed = results.size() - success;
        System.out.println("ConcurrentDbQueryDemo summary success=" + success + " failed=" + failed);
        for (Result r : results) {
            if (r.success) {
                System.out.println("ok sql=" + r.sql + " ms=" + r.durationMs + " data=" + r.data);
            } else {
                System.out.println("fail sql=" + r.sql + " ms=" + r.durationMs + " error=" + r.error);
            }
        }
        executor.shutdown();
    }

    private static Result query(String sql, long latencyMs, boolean fail) {
        Instant start = Instant.now();
        try {
            TimeUnit.MILLISECONDS.sleep(latencyMs);
            if (fail) throw new IllegalStateException("db error");
            String data = "rows=" + (int)(latencyMs % 5 + 1);
            long ms = Duration.between(start, Instant.now()).toMillis();
            return Result.ok(sql, data, ms);
        } catch (InterruptedException e) {
            long ms = Duration.between(start, Instant.now()).toMillis();
            Thread.currentThread().interrupt();
            return Result.failed(sql, new RuntimeException("interrupted"), ms);
        }
    }

    private static class Result {
        final String sql;
        final boolean success;
        final String data;
        final String error;
        final long durationMs;
        private Result(String sql, boolean success, String data, String error, long durationMs) {
            this.sql = sql;
            this.success = success;
            this.data = data;
            this.error = error;
            this.durationMs = durationMs;
        }
        static Result ok(String sql, String data, long ms) {
            return new Result(sql, true, data, null, ms);
        }
        static Result failed(String sql, Throwable ex) {
            return new Result(sql, false, null, ex.getClass().getSimpleName() + ":" + ex.getMessage(), 0);
        }
        static Result failed(String sql, Throwable ex, long ms) {
            return new Result(sql, false, null, ex.getClass().getSimpleName() + ":" + ex.getMessage(), ms);
        }
    }
}
