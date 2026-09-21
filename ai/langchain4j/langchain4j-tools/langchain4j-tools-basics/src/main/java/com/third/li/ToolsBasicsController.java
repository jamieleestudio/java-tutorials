package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 工具调用接口：GET /ai/tools */
@RestController
public class ToolsBasicsController {

    private final ToolsConfig.ToolAssistant assistant;

    public ToolsBasicsController(ToolsConfig.ToolAssistant assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/ai/tools")
    public String ask(
            @RequestParam(defaultValue = "杭州今天天气怎么样？另外帮我算一下 256 乘以 4") String message) {
        return assistant.chat(message);
    }
}
