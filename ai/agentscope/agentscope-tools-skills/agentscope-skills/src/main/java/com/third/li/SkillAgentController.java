package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class SkillAgentController {

    private final SkillAgent agent;

    public SkillAgentController(SkillAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/skills/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "技能系统：从文件加载技能定义，动态注入提示词") String message) {
        return agent.chat(message);
    }
}