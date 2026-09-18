package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 剧本模式接口。 */
@RestController
public class PlaybookAgentController {

    private final PlaybookAgent agent;

    public PlaybookAgentController(PlaybookAgent agent) {
        this.agent = agent;
    }

    /** 根据用户需求选择对应剧本执行。 */
    @GetMapping("/patterns/playbook/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "发布新版本 v2.1.0") String message) {
        return agent.chat(message);
    }
}