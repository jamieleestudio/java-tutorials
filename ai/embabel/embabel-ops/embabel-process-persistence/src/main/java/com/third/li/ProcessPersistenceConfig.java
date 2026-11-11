package com.third.li;

import com.embabel.agent.spi.persistence.SerializedAgentProcessSnapshot;
import com.embabel.agent.spi.persistence.StoredSnapshotMetadata;
import com.embabel.agent.spi.persistence.AgentProcessSnapshotStore;
import com.embabel.agent.spi.support.persistence.InMemoryAgentProcessSnapshotStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Agent 进程快照持久化（Embabel 1.5.2+ 新特性）。
 *
 * <p>1.5.2 给 Agent 引入了"进程"级别的持久化：只要 Spring 上下文里存在一个
 * {@link AgentProcessSnapshotStore} Bean，平台就会按 checkpoint 策略
 * （{@code AgentProcessCheckpointPolicy}）把每个 AgentProcess 的黑板/状态
 * 序列化成快照存入 store —— 这是 HITL（人机协同）跨重启恢复、进程审计的基础。
 *
 * <p>官方提供三层实现：
 * <ul>
 *   <li>{@link InMemoryAgentProcessSnapshotStore}（本模块使用，零依赖）</li>
 *   <li>Cache-backed（JCache/Caffeine，由 {@code embabel-agent-cache-core} 自动装配）</li>
 *   <li>JDBC / Blackboard prod 持久化（1.5.3，需要数据库）</li>
 * </ul>
 *
 * <p>这里用 {@link RecordingSnapshotStore} 包装内存实现，把"平台确实在写快照"
 * 这件事暴露成 HTTP 接口：跑一次 Agent，就能看到快照元数据（processId / 版本 / 状态）。
 */
@Configuration
public class ProcessPersistenceConfig {

    private static final Logger log = LoggerFactory.getLogger(ProcessPersistenceConfig.class);

    /** 记录型包装：委托内存实现，同时登记每次 save 的元数据，供 HTTP 观测。 */
    public static class RecordingSnapshotStore implements AgentProcessSnapshotStore {

        private final InMemoryAgentProcessSnapshotStore delegate = new InMemoryAgentProcessSnapshotStore();
        private final Map<String, StoredSnapshotMetadata> recorded = new LinkedHashMap<>();

        @Override
        public StoredSnapshotMetadata save(SerializedAgentProcessSnapshot snapshot, Long expectedVersion) {
            StoredSnapshotMetadata metadata = delegate.save(snapshot, expectedVersion);
            recorded.put(snapshot.getProcessId(), metadata);
            log.info("snapshot saved: processId={}, agent={}, status={}, version={}",
                    snapshot.getProcessId(), snapshot.getAgentName(), snapshot.getStatus(), snapshot.getVersion());
            return metadata;
        }

        @Override
        public SerializedAgentProcessSnapshot findLatestByProcessId(String processId) {
            return delegate.findLatestByProcessId(processId);
        }

        @Override
        public java.util.List<SerializedAgentProcessSnapshot> findByParentId(String parentId) {
            return delegate.findByParentId(parentId);
        }

        @Override
        public void delete(String processId) {
            recorded.remove(processId);
            delegate.delete(processId);
        }

        public Map<String, StoredSnapshotMetadata> recorded() {
            return recorded;
        }
    }

    @Bean
    public AgentProcessSnapshotStore agentProcessSnapshotStore() {
        // 存在这个 Bean = 持久化激活；换成 JCache/JDBC 实现即可无缝升级
        return new RecordingSnapshotStore();
    }
}
