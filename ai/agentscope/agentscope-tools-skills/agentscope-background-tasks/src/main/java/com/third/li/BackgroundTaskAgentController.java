package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class BackgroundTaskAgentController {

    private final BackgroundTaskAgent agent;

    public BackgroundTaskAgentController(BackgroundTaskAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/background/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "后台任务：长任务移到后台，完成后唤醒 Agent") String message) {
        return agent.chat(message);
    }
}