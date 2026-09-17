package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class HitlConfirmAgentController {

    private final HitlConfirmAgent agent;

    public HitlConfirmAgentController(HitlConfirmAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/hitl/confirm")
    public String ask(
            @RequestParam(value = "message", defaultValue = "人工确认：危险操作前发 RequireUserConfirmEvent 等用户确认") String message) {
        return agent.chat(message);
    }
}