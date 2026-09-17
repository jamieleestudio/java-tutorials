package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class SchedulerAgentController {

    private final SchedulerAgent agent;

    public SchedulerAgentController(SchedulerAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/scheduler/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "定时调度：定时唤醒 Agent 执行任务") String message) {
        return agent.chat(message);
    }
}