package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 并行模式接口：GET /patterns/parallel/ask
 */
@RestController
public class PatternParallelController {

    private final PatternParallelService patternService;

    public PatternParallelController(PatternParallelService patternService) {
        this.patternService = patternService;
    }

    @GetMapping("/patterns/parallel/ask")
    public Map<String, Object> ask(
            @RequestParam(value = "message",
                    defaultValue = "给公司内部知识库加一个 AI 问答机器人")
            String message) throws Exception {
        return patternService.run(message);
    }
}
