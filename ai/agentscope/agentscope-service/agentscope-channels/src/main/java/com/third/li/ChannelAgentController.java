package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class ChannelAgentController {

    private final ChannelAgent agent;

    public ChannelAgentController(ChannelAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/channels/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "IM 渠道：接入钉钉/飞书/企微") String message) {
        return agent.chat(message);
    }
}