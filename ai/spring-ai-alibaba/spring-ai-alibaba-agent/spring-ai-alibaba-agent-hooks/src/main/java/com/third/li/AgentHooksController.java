package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hook 体系接口。
 *
 * GET /agent/hooks/summary?round=…&message=… — 上下文摘要演示（同一会话多轮）。
 * GET /agent/hooks/pii?message=… — PII 打码演示。
 */
@RestController
public class AgentHooksController {

    private final AgentHooksService service;

    public AgentHooksController(AgentHooksService service) {
        this.service = service;
    }

    @GetMapping("/agent/hooks/summary")
    public String summary(
            @RequestParam(value = "round", defaultValue = "1") Long round,
            @RequestParam(value = "message", defaultValue = "补充一个技术细节：Graph 的 KeyStrategy 决定状态合并方式")
            String message) throws Exception {
        return service.chat(round, message);
    }

    @GetMapping("/agent/hooks/pii")
    public String pii(
            @RequestParam(value = "message",
                    defaultValue = "请给我发送确认邮件到 zhangsan@example.com，谢谢")
            String message) throws Exception {
        return service.pii(message);
    }
}
