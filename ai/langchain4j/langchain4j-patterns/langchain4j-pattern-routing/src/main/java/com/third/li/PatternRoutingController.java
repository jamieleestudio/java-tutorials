package com.third.li;

import dev.langchain4j.agentic.UntypedAgent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 路由模式接口：GET /patterns/routing?request=… */
@RestController
public class PatternRoutingController {

    private final UntypedAgent expertsWorkflow;

    public PatternRoutingController(UntypedAgent expertsWorkflow) {
        this.expertsWorkflow = expertsWorkflow;
    }

    @GetMapping("/patterns/routing")
    public String ask(@RequestParam(defaultValue = "我的膝盖在跑步后疼痛，应该怎么处理？") String request) {
        return (String) expertsWorkflow.invoke(java.util.Map.of("request", request));
    }
}
