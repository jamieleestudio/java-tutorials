package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class SnapshotAgentController {

    private final SnapshotAgent agent;

    public SnapshotAgentController(SnapshotAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/workspace/snapshot")
    public String ask(
            @RequestParam(value = "message", defaultValue = "工作区快照：保存和恢复工作区状态") String message) {
        return agent.chat(message);
    }
}