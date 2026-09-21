package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 多轮记忆接口：同一实例连续对话验证上下文留存。 */
@RestController
public class ChatMemoryController {

    private final ChatMemoryConfig.ChatAssistant assistant;

    public ChatMemoryController(ChatMemoryConfig.ChatAssistant assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/ai/memory/round1")
    public String round1(@RequestParam(defaultValue = "你好，我叫小李，最喜欢 Java") String message) {
        return assistant.chat(message);
    }

    @GetMapping("/ai/memory/round2")
    public String round2(@RequestParam(defaultValue = "我叫什么名字？最喜欢什么技术？") String message) {
        return assistant.chat(message);
    }
}
