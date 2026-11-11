package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.persistence.SerializedAgentProcessSnapshot;
import com.embabel.agent.spi.persistence.StoredSnapshotMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 进程快照持久化接口。
 *
 * <p>POST /persistence/run 跑一次两步 Agent（平台在 checkpoint 处自动写快照）；
 * GET /persistence/snapshots 查看已登记的快照元数据；
 * GET /persistence/snapshots/{processId} 查看某进程最新快照的细节（状态/版本/载荷大小）。
 */
@RestController
public class ProcessPersistenceController {

    private final AgentPlatform agentPlatform;
    private final ProcessPersistenceConfig.RecordingSnapshotStore snapshotStore;

    public ProcessPersistenceController(AgentPlatform agentPlatform,
                                        ProcessPersistenceConfig.RecordingSnapshotStore snapshotStore) {
        this.agentPlatform = agentPlatform;
        this.snapshotStore = snapshotStore;
    }

    @PostMapping("/persistence/run")
    public Map<String, Object> run(
            @RequestParam(defaultValue = "Agent 进程快照持久化的价值") String topic) {
        String result = AgentInvocation.create(agentPlatform, String.class)
                .invoke(new UserInput(topic));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", result);
        response.put("recordedProcesses", snapshotStore.recorded().keySet());
        return response;
    }

    @GetMapping("/persistence/snapshots")
    public Map<String, StoredSnapshotMetadata> snapshots() {
        return snapshotStore.recorded();
    }

    @GetMapping("/persistence/snapshots/{processId}")
    public Map<String, Object> snapshot(@PathVariable String processId) {
        SerializedAgentProcessSnapshot snapshot = snapshotStore.findLatestByProcessId(processId);
        if (snapshot == null) {
            return Map.of("processId", processId, "error", "未找到快照");
        }
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("processId", snapshot.getProcessId());
        info.put("agent", snapshot.getAgentName());
        info.put("status", String.valueOf(snapshot.getStatus()));
        info.put("version", snapshot.getVersion());
        info.put("payloadBytes", snapshot.getPayload().length);
        info.put("updatedAt", String.valueOf(snapshot.getUpdatedAt()));
        return info;
    }
}
