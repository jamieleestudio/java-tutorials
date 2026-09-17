package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class ToolGroupAgentController {

    private final ToolGroupAgent agent;

    public ToolGroupAgentController(ToolGroupAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/toolgroup/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "工具分组：把工具按 read/write 分组，模型只能看到当前组") String message) {
        return agent.chat(message);
    }
}