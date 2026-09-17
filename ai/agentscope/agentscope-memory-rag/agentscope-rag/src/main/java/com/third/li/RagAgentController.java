package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class RagAgentController {

    private final RagAgent agent;

    public RagAgentController(RagAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/rag/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "RAG 检索：从知识库检索相关片段注入提示词") String message) {
        return agent.chat(message);
    }
}