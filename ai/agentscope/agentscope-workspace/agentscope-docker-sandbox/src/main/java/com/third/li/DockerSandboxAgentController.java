package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Docker 沙箱接口。 */
@RestController
public class DockerSandboxAgentController {

    private final DockerSandboxAgent agent;

    public DockerSandboxAgentController(DockerSandboxAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/sandbox/ask")
    public String ask(
            @RequestParam(value = "message", defaultValue = "在沙箱中执行 Java 代码") String message) {
        return agent.chat(message);
    }

    /** 查看 Docker 沙箱配置。 */
    @GetMapping("/sandbox/config")
    public String config() {
        return agent.describeSandbox();
    }
}