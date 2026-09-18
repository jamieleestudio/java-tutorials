package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 状态机接口。 */
@RestController
public class StateMachineAgentController {

    private final StateMachineAgent agent;

    public StateMachineAgentController(StateMachineAgent agent) {
        this.agent = agent;
    }

    /** 计划模式（PLAN 状态）下对话：只读分析。 */
    @GetMapping("/patterns/state-machine/plan")
    public String plan(
            @RequestParam(value = "message", defaultValue = "分析这个项目的技术选型") String message) {
        return agent.chat(message, true);
    }

    /** 执行模式（ACT 状态）下对话：可操作。 */
    @GetMapping("/patterns/state-machine/act")
    public String act(
            @RequestParam(value = "message", defaultValue = "创建项目文件") String message) {
        return agent.chat(message, false);
    }

    /** 查看当前状态。 */
    @GetMapping("/patterns/state-machine/status")
    public String status() {
        return agent.status();
    }
}