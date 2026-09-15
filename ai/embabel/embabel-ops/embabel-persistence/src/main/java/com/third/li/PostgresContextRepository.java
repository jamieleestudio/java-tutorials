package com.third.li;

import com.embabel.agent.core.Context;
import com.embabel.agent.spi.ContextRepository;
import com.embabel.agent.spi.support.InMemoryContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Postgres 版 {@link ContextRepository}：把上下文（用户/会话级的长期状态）持久化到数据库，
 * 应用重启后仍能读回。
 *
 * <p>实现要点：
 * <ul>
 *   <li>复用框架的 {@link InMemoryContext} 作为"上下文对象"，本类只负责**存取**</li>
 *   <li>序列化时给每个对象带上 {@code type}（全限定类名），读回时按类型还原，
 *       这样强类型对象（record/POJO）不会退化成 Map</li>
 *   <li>框架默认的 {@code contextRepository} Bean 没有 {@code @ConditionalOnMissingBean}，
 *       所以这里用 {@link Primary} 覆盖它的注入优先级</li>
 * </ul>
 *
 * <p>说明：{@code AgentProcessRepository}（进程仓库）不适合这样持久化——{@code AgentProcess}
 * 包含黑板、规划器、历史等运行期对象，无法可靠地序列化重建。生产上如需进程恢复，
 * 通常走事件溯源（把过程事件写库，重启后重放），框架提供
 * {@code AbstractAgentProcessRepository} 作为扩展点。
 */
@Component
@Primary
public class PostgresContextRepository implements ContextRepository {

    private static final Logger log = LoggerFactory.getLogger(PostgresContextRepository.class);

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public PostgresContextRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS embabel_context (
                    id VARCHAR(255) PRIMARY KEY,
                    payload TEXT NOT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT now()
                )
                """);
        log.info("embabel_context table ready");
    }

    @Override
    public Context create() {
        return new InMemoryContext(UUID.randomUUID().toString());
    }

    @Override
    public Context createWithId(String id) {
        return new InMemoryContext(id);
    }

    @Override
    public Context save(Context context) {
        jdbc.update("""
                INSERT INTO embabel_context (id, payload, updated_at)
                VALUES (?, ?, now())
                ON CONFLICT (id) DO UPDATE SET payload = EXCLUDED.payload, updated_at = now()
                """, context.getId(), serialize(context.getObjects()));
        log.info("Saved context {} ({} objects)", context.getId(), context.getObjects().size());
        return context;
    }

    @Override
    public Context findById(String id) {
        List<String> payloads = jdbc.queryForList(
                "SELECT payload FROM embabel_context WHERE id = ?", String.class, id);
        if (payloads.isEmpty()) {
            return null;
        }
        Context context = new InMemoryContext(id);
        deserialize(payloads.get(0)).forEach(context::addObject);
        return context;
    }

    @Override
    public void delete(Context context) {
        jdbc.update("DELETE FROM embabel_context WHERE id = ?", context.getId());
        log.info("Deleted context {}", context.getId());
    }

    private String serialize(List<Object> objects) {
        ArrayNode array = objectMapper.createArrayNode();
        for (Object object : objects) {
            ObjectNode node = array.addObject();
            node.put("type", object.getClass().getName());
            node.set("value", objectMapper.valueToTree(object));
        }
        return array.toString();
    }

    private List<Object> deserialize(String payload) {
        List<Object> objects = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(payload);
            for (JsonNode node : root) {
                String type = node.get("type").asText();
                try {
                    objects.add(objectMapper.treeToValue(node.get("value"), Class.forName(type)));
                } catch (Exception e) {
                    log.warn("Failed to restore object of type {}: {}", type, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse context payload: {}", e.getMessage());
        }
        return objects;
    }
}
