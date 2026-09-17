package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 上下文压缩接口。 */
@RestController
public class ContextCompactionAgentController {

    private final ContextCompactionAgent agent;

    public ContextCompactionAgentController(ContextCompactionAgent agent) {
        this.agent = agent;
    }

    /**
     * 多轮对话：当消息数超过 8 条或 token 超过 2000 时，
     * CompactionMiddleware 自动压缩旧消息为摘要。
     *
     * <p>试着连续调用多次，观察日志中的 compaction 触发。
     */
    @GetMapping("/compaction/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "用一句话介绍上下文压缩") String message) {
        return agent.chat(message);
    }
}