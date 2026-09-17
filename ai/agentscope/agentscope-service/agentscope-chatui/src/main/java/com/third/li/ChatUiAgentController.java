package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** ChatUI 接口。 */
@RestController
public class ChatUiAgentController {

    private final ChatUiAgent agent;

    public ChatUiAgentController(ChatUiAgent agent) {
        this.agent = agent;
    }

    /** 发送息。 */
    @GetMapping("/chatui/send")
    public String send(
            @RequestParam(value = "message", defaultValue = "你好，请自我介绍") String message) {
        return agent.send(message);
    }

    /** 轮询回复。 */
    @GetMapping("/chatui/poll")
    public String poll() {
        return agent.pollOutbound();
    }
}