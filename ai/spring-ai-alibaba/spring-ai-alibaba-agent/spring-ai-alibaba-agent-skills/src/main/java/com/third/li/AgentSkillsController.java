package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 技能体系接口：GET /agent/skills/ask
 */
@RestController
public class AgentSkillsController {

    private final AgentSkillsService service;

    public AgentSkillsController(AgentSkillsService service) {
        this.service = service;
    }

    @GetMapping("/agent/skills/ask")
    public String ask(
            @RequestParam(value = "message",
                    defaultValue = "用发布说明的格式写一下：本次新增了 Graph 图编排能力，修复了流式输出偶发截断的问题")
            String message) throws Exception {
        return service.ask(message);
    }
}
