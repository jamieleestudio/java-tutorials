package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class ChatUiAgentController {

    private final ChatUiAgent agent;

    public ChatUiAgentController(ChatUiAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/chatui/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "ChatUI：内置 Web UI 渠道") String message) {
        return agent.chat(message);
    }
}