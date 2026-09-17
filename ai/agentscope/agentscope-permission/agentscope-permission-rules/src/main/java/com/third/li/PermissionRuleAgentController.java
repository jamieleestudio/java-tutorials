package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class PermissionRuleAgentController {

    private final PermissionRuleAgent agent;

    public PermissionRuleAgentController(PermissionRuleAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/permission/rules")
    public String ask(
            @RequestParam(value = "message", defaultValue = "权限规则：按路径/命令名定义允许或拒绝规则") String message) {
        return agent.chat(message);
    }
}