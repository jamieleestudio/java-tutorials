package com.third.li;

import dev.langchain4j.agentic.UntypedAgent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 循环工作流接口：GET /ai/agentic/loop?topic=…&style=…
 */
@RestController
public class AgenticLoopController {

    private final UntypedAgent styleReviewLoop;

    public AgenticLoopController(UntypedAgent styleReviewLoop) {
        this.styleReviewLoop = styleReviewLoop;
    }

    @GetMapping("/ai/agentic/loop")
    public String loop(
            @RequestParam(defaultValue = "一个太空歌剧的冒险故事") String topic,
            @RequestParam(defaultValue = "幽默") String style) {
        // 先产出初稿，再进入打分-改写循环
        Object story = styleReviewLoop.invoke(java.util.Map.of(
                "topic", topic,
                "style", style));
        return String.valueOf(story);
    }
}
