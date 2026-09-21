package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 护栏接口：GET /ai/guardrails?message=…（包含手机号会被打码）。 */
@RestController
public class GuardrailsController {

    private final GuardrailsConfig.SafeAssistant assistant;

    public GuardrailsController(GuardrailsConfig.SafeAssistant assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/ai/guardrails")
    public String chat(
            @RequestParam(defaultValue = "我的手机号是 13812345678，帮我注册会员") String message) {
        return assistant.chat(message);
    }
}
