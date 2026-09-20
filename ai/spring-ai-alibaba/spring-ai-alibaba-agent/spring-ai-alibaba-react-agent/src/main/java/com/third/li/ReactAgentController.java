package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReactAgent 接口。
 *
 * GET /agent/ask?message=… — 调用 ReAct 智能体。
 * GET /agent/mermaid — 输出 Agent 底层图的 Mermaid 定义。
 */
@RestController
public class ReactAgentController {

    private final ReactAgentService agentService;

    public ReactAgentController(ReactAgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/agent/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "用 Spring AI Alibaba 做 RAG 需要哪些组件？")
            String message) throws Exception {
        return agentService.ask(message);
    }

    @GetMapping(value = "/agent/mermaid", produces = "text/plain;charset=UTF-8")
    public String mermaid() {
        return agentService.mermaid();
    }
}
