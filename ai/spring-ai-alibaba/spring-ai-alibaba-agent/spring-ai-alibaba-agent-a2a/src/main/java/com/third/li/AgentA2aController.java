package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * A2A 互操作接口：GET /agent/a2a/ask?baseUrl=…&amp;message=…
 */
@RestController
public class AgentA2aController {

    private final AgentA2aService service;

    public AgentA2aController(AgentA2aService service) {
        this.service = service;
    }

    @GetMapping("/agent/a2a/ask")
    public Map<String, Object> ask(
            @RequestParam(value = "baseUrl",
                    defaultValue = "http://localhost:8080") String baseUrl,
            @RequestParam(value = "message", defaultValue = "你好，请做一下自我介绍")
            String message) {
        try {
            return service.ask(baseUrl, message);
        } catch (Exception e) {
            return Map.of(
                    "error", "A2A 调用失败：" + e.getMessage(),
                    "hint", "需要一个可达的 A2A server（如 SAA Studio / a2a-nacos 注册的 Agent）。"
                            + "用 ?baseUrl= 指向其基地址。");
        }
    }
}
