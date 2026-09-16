package com.third.li;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 块与向量的持久化（Postgres + pgvector，直接 JDBC）。
 *
 * <p>两张表：
 * <ul>
 *   <li>{@code doc_source} —— **文档级**信息（含内容哈希），增量摄入靠它判断"要不要重跑"</li>
 *   <li>{@code doc_chunk} —— **块级**信息 + 向量，检索靠它</li>
 * </ul>
 *
 * <p>为什么分成两级：重新摄入一份文档时，需要按 {@code doc_id} 一次性删掉它的旧块，
 * 再写入新块——如果只有块表，"这份文档现在有几个块"就得靠聚合查询，容易写错。
 */
@Component
public class ChunkStore {

    private static final Logger log = LoggerFactory.getLogger(ChunkStore.class);

    private final JdbcTemplate jdbc;
    private final int dim;
    private final String table;
    private final String docTable;

    private volatile boolean schemaReady;

    public ChunkStore(
            JdbcTemplate jdbc,
            @Value("${app.vector.dim:768}") int dim,
            @Value("${app.vector.table:ingest_chunk}") String table,
            @Value("${app.vector.doc-table:ingest_doc}") String docTable) {
        this.jdbc = jdbc;
        this.dim = dim;
        this.table = table;
        this.docTable = docTable;
    }

    public int dim() {
        return dim;
    }

    public synchronized void ensureSchema() {
        if (schemaReady) {
            return;
        }
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS %s (
                  doc_id       TEXT PRIMARY KEY,
                  source       TEXT NOT NULL,
                  title        TEXT NOT NULL,
                  content_hash TEXT NOT NULL,
                  chunk_count  INTEGER NOT NULL,
                  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
                )
                """.formatted(docTable));
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS %s (
                  chunk_id     TEXT PRIMARY KEY,
                  doc_id       TEXT NOT NULL,
                  source       TEXT NOT NULL,
                  title        TEXT NOT NULL,
                  seq          INTEGER NOT NULL,
                  text         TEXT NOT NULL,
                  embedding    vector(%d) NOT NULL
                )
                """.formatted(table, dim));
        jdbc.execute("""
                CREATE INDEX IF NOT EXISTS %s_doc_id_idx ON %s (doc_id)
                """.formatted(table, table));
        jdbc.execute("""
                CREATE INDEX IF NOT EXISTS %s_embedding_idx
                ON %s USING hnsw (embedding vector_cosine_ops)
                """.formatted(table, table));
        schemaReady = true;
        log.info("pgvector schema ready: table={}, dim={}", table, dim);
    }

    /** 已入库的文档内容哈希；不存在则返回 null。 */
    public String findContentHash(String docId) {
        ensureSchema();
        List<String> hashes = jdbc.queryForList(
                "SELECT content_hash FROM " + docTable + " WHERE doc_id = ?", String.class, docId);
        return hashes.isEmpty() ? null : hashes.get(0);
    }

    /** 用新块整体替换某文档的旧块（先删后插，保证不会残留旧内容）。 */
    public void replaceDocument(RawDocument document, List<Chunk> chunks, List<float[]> embeddings) {
        ensureSchema();
        if (chunks.size() != embeddings.size()) {
            throw new IllegalArgumentException("块数与向量数不一致");
        }
        jdbc.update("DELETE FROM " + table + " WHERE doc_id = ?", document.docId());
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            float[] embedding = embeddings.get(i);
            if (embedding.length != dim) {
                throw new IllegalArgumentException(
                        "嵌入维度 %d 与配置的 app.vector.dim=%d 不一致".formatted(embedding.length, dim));
            }
            jdbc.update("""
                    INSERT INTO %s (chunk_id, doc_id, source, title, seq, text, embedding)
                    VALUES (?, ?, ?, ?, ?, ?, ?::vector)
                    ON CONFLICT (chunk_id) DO UPDATE
                      SET text = EXCLUDED.text, embedding = EXCLUDED.embedding
                    """.formatted(table),
                    chunk.chunkId(), chunk.docId(), chunk.source(), chunk.title(),
                    chunk.seq(), chunk.text(), toLiteral(embedding));
        }
        jdbc.update("""
                INSERT INTO %s (doc_id, source, title, content_hash, chunk_count, updated_at)
                VALUES (?, ?, ?, ?, ?, now())
                ON CONFLICT (doc_id) DO UPDATE
                  SET source = EXCLUDED.source, title = EXCLUDED.title,
                      content_hash = EXCLUDED.content_hash, chunk_count = EXCLUDED.chunk_count,
                      updated_at = now()
                """.formatted(docTable),
                document.docId(), document.source(), document.title(),
                document.contentHash(), chunks.size());
        log.info("已摄入 {}：{} 块", document.source(), chunks.size());
    }

    /** 近邻检索。 */
    public List<ChunkHit> search(float[] query, int k) {
        ensureSchema();
        String literal = toLiteral(query);
        return jdbc.query("""
                SELECT chunk_id, doc_id, source, title, seq, text,
                       1 - (embedding <=> ?::vector) AS score
                FROM %s
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """.formatted(table),
                (rs, rowNum) -> new ChunkHit(
                        rs.getString("chunk_id"),
                        rs.getString("doc_id"),
                        rs.getString("source"),
                        rs.getString("title"),
                        rs.getInt("seq"),
                        rs.getString("text"),
                        rs.getDouble("score")),
                literal, literal, k);
    }

    public int countChunks() {
        ensureSchema();
        Integer n = jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class);
        return n == null ? 0 : n;
    }

    public int countDocuments() {
        ensureSchema();
        Integer n = jdbc.queryForObject("SELECT count(*) FROM " + docTable, Integer.class);
        return n == null ? 0 : n;
    }

    /** 一次检索命中的块。 */
    public record ChunkHit(
            String chunkId, String docId, String source, String title,
            int seq, String text, double score) {
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
