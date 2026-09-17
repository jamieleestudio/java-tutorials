package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 结构化输出接口。 */
@RestController
public class StructuredOutputController {

    private final StructuredOutputAgent agent;

    public StructuredOutputController(StructuredOutputAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/structured/analyze")
    public ProductAnalysis analyze(
            @RequestParam(value = "product", defaultValue = "DeepSeek V4 Flash 大模型") String product) {
        return agent.analyze(product);
    }
}