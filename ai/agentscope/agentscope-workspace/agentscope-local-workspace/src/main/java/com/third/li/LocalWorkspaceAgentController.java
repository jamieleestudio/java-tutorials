package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class LocalWorkspaceAgentController {

    private final LocalWorkspaceAgent agent;

    public LocalWorkspaceAgentController(LocalWorkspaceAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/workspace/local")
    public String ask(
            @RequestParam(value = "message", defaultValue = "本地工作区：Agent 在指定目录读写文件") String message) {
        return agent.chat(message);
    }
}