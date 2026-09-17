package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 实时打断接口。 */
@RestController
public class InterruptAgentController {

    private final InterruptAgent agent;

    public InterruptAgentController(InterruptAgent agent) {
        this.agent = agent;
    }

    /** 启动对话（异步）。 */
    @GetMapping("/interrupt/start")
    public String start(
            @RequestParam(value = "message", defaultValue = "请详细介绍 Java 并发编程的各个方面") String message) {
        return agent.startChat(message);
    }

    /** 打断当前对话。 */
    @GetMapping("/interrupt/stop")
    public String stop() {
        return agent.interrupt();
    }

    /** 查看状态。 */
    @GetMapping("/interrupt/status")
    public String status() {
        return agent.status();
    }
}