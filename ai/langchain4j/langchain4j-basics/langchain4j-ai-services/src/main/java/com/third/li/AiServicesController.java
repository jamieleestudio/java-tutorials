package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AiServices 声明式接口。
 *
 * GET /ai/services/chat — 模板化对话
 * GET /ai/services/translate — 翻译
 */
@RestController
public class AiServicesController {

    private final Assistant assistant;

    public AiServicesController(Assistant assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/ai/services/chat")
    public String chat(@RequestParam(defaultValue = "LangChain4j 的 AiServices 解决了什么问题？") String message) {
        return assistant.chat(message);
    }

    @GetMapping("/ai/services/translate")
    public String translate(@RequestParam(defaultValue = "声明式接口让 AI 调用像普通方法一样简单") String text) {
        return assistant.translate(text);
    }
}
