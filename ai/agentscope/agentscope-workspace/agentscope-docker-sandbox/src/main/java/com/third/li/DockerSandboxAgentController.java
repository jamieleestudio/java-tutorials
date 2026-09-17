package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class DockerSandboxAgentController {

    private final DockerSandboxAgent agent;

    public DockerSandboxAgentController(DockerSandboxAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/workspace/docker")
    public String ask(
            @RequestParam(value = "message", defaultValue = "Docker 沙箱：在容器里执行 Shell 命令，隔离主机环境") String message) {
        return agent.chat(message);
    }
}