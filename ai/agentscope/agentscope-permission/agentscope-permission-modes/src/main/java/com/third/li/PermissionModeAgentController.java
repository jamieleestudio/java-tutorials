package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class PermissionModeAgentController {

    private final PermissionModeAgent agent;

    public PermissionModeAgentController(PermissionModeAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/permission/modes")
    public String ask(
            @RequestParam(value = "message", defaultValue = "权限模式：bypass 不确认 / confirm 每次确认 / strict 严格规则") String message) {
        return agent.chat(message);
    }
}