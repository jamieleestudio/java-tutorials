package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 主管编排模式接口：GET /patterns/supervisor/ask
 */
@RestController
public class PatternSupervisorController {

    private final PatternSupervisorService patternService;

    public PatternSupervisorController(PatternSupervisorService patternService) {
        this.patternService = patternService;
    }

    @GetMapping("/patterns/supervisor/ask")
    public Map<String, Object> ask(
            @RequestParam(value = "message",
                    defaultValue = "写一段产品发布会的开场白，主题是 AI 赋能企业客服")
            String message) throws Exception {
        return patternService.run(message);
    }
}
