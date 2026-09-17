package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** RAG 后端接口。 */
@RestController
public class RagBackendAgentController {

    private final RagBackendsAgent agent;

    public RagBackendAgentController(RagBackendsAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/rag/backends")
    public String ask(
            @RequestParam(value = "message", defaultValue = "介绍 RAG 后端选择") String message) {
        return agent.chat(message);
    }

    /** 查看后端对比。 */
    @GetMapping("/rag/backends/compare")
    public String compare() {
        return agent.describeBackends();
    }
}