package com.third.li;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * **pgvector 持久化向量库**（直接 JDBC + 原生 SQL，不依赖 Spring AI 的 VectorStore 抽象）。
 *
 * <p>为什么单独写而不是复用 {@code embabel-embeddings} 的内存检索：
 * <ul>
 *   <li><b>持久化</b>：向量存在 Postgres 里，进程重启不丢，也不需要每次请求重算语料向量</li>
 *   <li><b>规模</b>：内存里只能全表扫描；pgvector 有 HNSW 索引，能在百万级向量上做近邻搜索</li>
 *   <li><b>元数据过滤</b>：可以把「相似度排序」和普通 SQL 过滤（{@code WHERE source = ?}）
 *       组合在一次查询里——这是内存余弦相似度做不到的</li>
 * </ul>
 *
 * <p>关键 SQL：
 * <pre>
 *   -- 距离算子：&lt;=&gt; 余弦距离、&lt;-&gt; L2、&lt;#&gt; 内积
 *   SELECT id, title, content, source, 1 - (embedding &lt;=&gt; ?::vector) AS score
 *   FROM doc_chunk
 *   WHERE source = ?
 *   ORDER BY embedding &lt;=&gt; ?::vector
 *   LIMIT ?
 * </pre>
 *
 * <p>注意 {@code ?::vector}：pgvector 支持从文本字面量 {@code [0.1,0.2,...]} 转换，
 * 所以参数直接传字符串即可。
 */
@Component
public class PgVectorStore {

    private static final Logger log = LoggerFactory.getLogger(PgVectorStore.class);

    private final JdbcTemplate jdbc;
    private final int dim;
    private final String table;

    private volatile boolean schemaReady;

    public PgVectorStore(
            JdbcTemplate jdbc,
            @Value("${app.vector.dim:768}") int dim,
            @Value("${app.vector.table:doc_chunk}") String table) {
        this.jdbc = jdbc;
        this.dim = dim;
        this.table = table;
    }

    public int dim() {
        return dim;
    }

    /** 建扩展、建表、建索引（幂等）。 */
    public synchronized void ensureSchema() {
        if (schemaReady) {
            return;
        }
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS %s (
                  id        TEXT PRIMARY KEY,
                  title     TEXT NOT NULL,
                  content   TEXT NOT NULL,
                  source    TEXT NOT NULL,
                  embedding vector(%d) NOT NULL
                )
                """.formatted(table, dim));
        jdbc.execute("""
                CREATE INDEX IF NOT EXISTS %s_embedding_idx
                ON %s USING hnsw (embedding vector_cosine_ops)
                """.formatted(table, table));
        schemaReady = true;
        log.info("pgvector schema ready: table={}, dim={}", table, dim);
    }

    /** 写入或更新一条文档向量（幂等，可重复 ingest）。 */
    public void upsert(DocChunk doc, float[] embedding) {
        ensureSchema();
        if (embedding.length != dim) {
            throw new IllegalArgumentException(
                    "嵌入维度 %d 与配置的 app.vector.dim=%d 不一致；请检查嵌入模型是否匹配"
                            .formatted(embedding.length, dim));
        }
        jdbc.update("""
                INSERT INTO %s (id, title, content, source, embedding)
                VALUES (?, ?, ?, ?, ?::vector)
                ON CONFLICT (id) DO UPDATE
                  SET title = EXCLUDED.title,
                      content = EXCLUDED.content,
                      source = EXCLUDED.source,
                      embedding = EXCLUDED.embedding
                """.formatted(table),
                doc.id(), doc.title(), doc.content(), doc.source(), toLiteral(embedding));
    }

    /**
     * 近邻检索。
     *
     * @param source 可选的元数据过滤（{@code null} 表示不过滤）
     */
    public List<VectorMatch> search(float[] query, int k, String source) {
        ensureSchema();
        String literal = toLiteral(query);
        boolean filtered = source != null && !source.isBlank();

        String sql = """
                SELECT id, title, content, source, 1 - (embedding <=> ?::vector) AS score
                FROM %s
                %s
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """.formatted(table, filtered ? "WHERE source = ?" : "");

        var mapper = (org.springframework.jdbc.core.RowMapper<VectorMatch>) (rs, rowNum) -> new VectorMatch(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("source"),
                rs.getDouble("score"));

        return filtered
                ? jdbc.query(sql, mapper, literal, source, literal, k)
                : jdbc.query(sql, mapper, literal, literal, k);
    }

    public int count() {
        ensureSchema();
        Integer n = jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class);
        return n == null ? 0 : n;
    }

    /** float[] -> pgvector 文本字面量 {@code [0.1,0.2,...]}。 */
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
