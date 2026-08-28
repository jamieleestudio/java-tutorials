package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天接口。通过 AgentScope 的 {@link ChatAgent} 以阻塞方式运行，
 * 并返回最终文本回复。
 */
@RestController
public class ChatController {

    private final ChatAgent chatAgent;

    public ChatController(ChatAgent chatAgent) {
        this.chatAgent = chatAgent;
    }

    @GetMapping("/ai/generate")
    public String generate(@RequestParam(value = "message", defaultValue = "讲个笑话") String message) {
        return chatAgent.chat(message);
    }
}