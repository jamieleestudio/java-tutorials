package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 持久化接口。 */
@RestController
public class PersistenceAgentController {

    private final PersistenceAgent agent;

    public PersistenceAgentController(PersistenceAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/persistence/chat")
    public String chat(
            @RequestParam(value = "message", defaultValue = "记住我的名字是小明") String message,
            @RequestParam(value = "session", defaultValue = "sess-1") String session) {
        return agent.chat(message, session);
    }

    @GetMapping("/persistence/clear")
    public String clear(
            @RequestParam(value = "session", defaultValue = "sess-1") String session) {
        return agent.clearSession(session);
    }

    @GetMapping("/persistence/sessions")
    public String sessions() {
        return agent.listSessions();
    }
}