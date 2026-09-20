package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Shell 工具接口：GET /agent/shell/ask、GET /agent/shell/hints
 */
@RestController
public class AgentShellController {

    private final AgentShellService service;

    public AgentShellController(AgentShellService service) {
        this.service = service;
    }

    @GetMapping("/agent/shell/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "看看 demo-workspace 目录里有什么文件，并统计 notes.md 有多少行")
            String message) throws Exception {
        return service.ask(message);
    }

    @GetMapping("/agent/shell/hints")
    public List<String> hints() {
        return service.hints();
    }
}
