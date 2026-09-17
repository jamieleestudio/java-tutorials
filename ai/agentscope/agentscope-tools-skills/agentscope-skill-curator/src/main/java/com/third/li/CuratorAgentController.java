package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 技能策展接口。 */
@RestController
public class CuratorAgentController {

    private final CuratorAgent agent;

    public CuratorAgentController(CuratorAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/curator/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "介绍技能策展系统") String message) {
        return agent.chat(message);
    }

    /** 手动触发一轮策展。 */
    @GetMapping("/curator/run")
    public String run() {
        return agent.runCurator();
    }

    /** 手动晋升技能。 */
    @GetMapping("/curator/promote")
    public String promote(
            @RequestParam(value = "name", defaultValue = "git-workflow") String name,
            @RequestParam(value = "reason", defaultValue = "频繁使用，自动晋升") String reason) {
        return agent.promoteSkill(name, reason);
    }
}