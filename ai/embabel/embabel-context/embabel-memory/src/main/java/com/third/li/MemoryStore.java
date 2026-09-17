package com.third.li;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 长期记忆的持久化（Postgres + pgvector，直接 JDBC）。
 *
 * <p>与 {@code embabel-vector-store} / {@code embabel-document-ingest} 同一套做法，
 * 区别在**表结构与检索条件**：
 * <ul>
 *   <li>多了一列 {@code user_id}，召回时**必须按用户过滤**（否则会串号）；</li>
 *   <li>多了一列 {@code kind}，区分"偏好"（preference）与"事实"（fact）——
 *       偏好通常比事实更稳定，可以在提示词里给更高权重。</li>
 * </ul>
 *
 * <p>为什么"长期记忆"不能只靠会话历史：会话历史在进程内存里（`embabel-conversation`），
 * 或者随上下文落库（`embabel-persistence`），但**都不会在新会话里被主动召回**。
 * 长期记忆要解决的是"上次说过的事，这次还知道"。
 */
@Component
public class MemoryStore {

    private static final Logger log = LoggerFactory.getLogger(MemoryStore.class);

    private final JdbcTemplate jdbc;
    private final int dim;
    private final String table;

    private volatile boolean schemaReady;

    public MemoryStore(
            JdbcTemplate jdbc,
            @Value("${app.vector.dim:768}") int dim,
            @Value("${app.memory.table:user_memory}") String table) {
        this.jdbc = jdbc;
        this.dim = dim;
        this.table = table;
    }

    public synchronized void ensureSchema() {
        if (schemaReady) {
            return;
        }
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS %s (
                  id         TEXT PRIMARY KEY,
                  user_id    TEXT NOT NULL,
                  kind       TEXT NOT NULL,
                  text       TEXT NOT NULL,
                  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                  embedding  vector(%d) NOT NULL
                )
                """.formatted(table, dim));
        jdbc.execute("""
                CREATE INDEX IF NOT EXISTS %s_user_idx ON %s (user_id)
                """.formatted(table, table));
        jdbc.execute("""
                CREATE INDEX IF NOT EXISTS %s_embedding_idx
                ON %s USING hnsw (embedding vector_cosine_ops)
                """.formatted(table, table));
        schemaReady = true;
        log.info("pgvector schema ready: table={}, dim={}", table, dim);
    }

    /** 写入一条记忆。 */
    public MemoryItem remember(String userId, String kind, String text, float[] embedding) {
        ensureSchema();
        if (embedding.length != dim) {
            throw new IllegalArgumentException(
                    "嵌入维度 %d 与配置的 app.vector.dim=%d 不一致".formatted(embedding.length, dim));
        }
        String id = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO %s (id, user_id, kind, text, embedding)
                VALUES (?, ?, ?, ?, ?::vector)
                """.formatted(table), id, userId, kind, text, toLiteral(embedding));
        log.info("记住：user={} kind={} text={}", userId, kind, text);
        return new MemoryItem(id, kind, text, 0.0);
    }

    /** 按用户召回最相关的 k 条记忆。 */
    public List<MemoryItem> recall(String userId, float[] query, int k) {
        ensureSchema();
        String literal = toLiteral(query);
        return jdbc.query("""
                SELECT id, kind, text, 1 - (embedding <=> ?::vector) AS score
                FROM %s
                WHERE user_id = ?
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """.formatted(table),
                (rs, rowNum) -> new MemoryItem(
                        rs.getString("id"),
                        rs.getString("kind"),
                        rs.getString("text"),
                        rs.getDouble("score")),
                literal, userId, literal, k);
    }

    public int forget(String userId) {
        ensureSchema();
        return jdbc.update("DELETE FROM " + table + " WHERE user_id = ?", userId);
    }

    public int count(String userId) {
        ensureSchema();
        Integer n = jdbc.queryForObject(
                "SELECT count(*) FROM " + table + " WHERE user_id = ?", Integer.class, userId);
        return n == null ? 0 : n;
    }

    private String toLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder(vector.length * 8 + 2).append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        return sb.append(']').toString();
    }
}
