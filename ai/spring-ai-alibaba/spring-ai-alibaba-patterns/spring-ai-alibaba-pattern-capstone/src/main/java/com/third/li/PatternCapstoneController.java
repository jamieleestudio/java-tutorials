package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 模式综合接口：GET /patterns/capstone/ask
 *
 * 试两个任务：?message=写一段关于深夜城市的文字（走 story 并行+打磨分支）
 *            ?message=长江有多长（走 answer 分支）
 */
@RestController
public class PatternCapstoneController {

    private final PatternCapstoneService patternService;

    public PatternCapstoneController(PatternCapstoneService patternService) {
        this.patternService = patternService;
    }

    @GetMapping("/patterns/capstone/ask")
    public Map<String, Object> ask(
            @RequestParam(value = "message", defaultValue = "写一段关于深夜城市的文字")
            String message) throws Exception {
        return patternService.run(message);
    }
}
