package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** OpenAI Responses API 接口：GET /responses/chat */
@RestController
public class ResponsesApiController {

    private final ResponsesApiAgent responsesApiAgent;

    public ResponsesApiController(ResponsesApiAgent responsesApiAgent) {
        this.responsesApiAgent = responsesApiAgent;
    }

    /** 走 OpenAI Responses 端点的一次对话。 */
    @GetMapping("/responses/chat")
    public Map<String, Object> chat(
            @RequestParam(defaultValue = "用一句话介绍 OpenAI Responses API 和 Chat Completions 的区别") String message) {
        return Map.of("message", message, "answer", responsesApiAgent.chat(message));
    }
}
