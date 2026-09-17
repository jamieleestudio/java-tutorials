package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 技能系统接口。 */
@RestController
public class SkillAgentController {

    private final SkillAgent agent;

    public SkillAgentController(SkillAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/skills/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "帮我提交代码") String message) {
        return agent.chat(message);
    }

    /** 列出已注册的技能。 */
    @GetMapping("/skills/list")
    public String list() {
        return agent.listSkills();
    }

    /** 注入示例技能。 */
    @GetMapping("/skills/seed")
    public String seed() {
        return agent.seedSkill();
    }
}