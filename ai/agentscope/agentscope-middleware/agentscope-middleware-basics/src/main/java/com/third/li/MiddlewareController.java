package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** 中间件接口。 */
@RestController
public class MiddlewareController {

    private final MiddlewareAgent agent;

    public MiddlewareController(MiddlewareAgent agent) {
        this.agent = agent;
    }

    /** 带中间件：系统提示被改写（回答末尾会有「——由中间件增强」）。 */
    @GetMapping("/middleware/enhanced")
    public String enhanced(
            @RequestParam(value = "message", defaultValue = "用一句话介绍中间件模式") String message) {
        return agent.chat(message);
    }

    /** 不带中间件（对照组）：正常回答。 */
    @GetMapping("/middleware/plain")
    public String plain(
            @RequestParam(value = "message", defaultValue = "用一句话介绍中间件模式") String message) {
        return agent.chatWithoutMiddleware(message);
    }
}