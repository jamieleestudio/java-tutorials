package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class RagBackendAgentController {

    private final RagBackendAgent agent;

    public RagBackendAgentController(RagBackendAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/rag/backends")
    public String ask(
            @RequestParam(value = "message", defaultValue = "RAG 后端：使用 extensions-rag-simple 做简单向量检索") String message) {
        return agent.chat(message);
    }
}