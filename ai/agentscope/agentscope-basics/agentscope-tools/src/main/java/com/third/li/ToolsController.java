package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 工具调用接口。 */
@RestController
public class ToolsController {

    private final ToolsAgent toolsAgent;

    public ToolsController(ToolsAgent toolsAgent) {
        this.toolsAgent = toolsAgent;
    }

    @GetMapping("/tools/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "帮我查一下订单 A1001 的状态") String message) {
        return toolsAgent.chat(message);
    }
}