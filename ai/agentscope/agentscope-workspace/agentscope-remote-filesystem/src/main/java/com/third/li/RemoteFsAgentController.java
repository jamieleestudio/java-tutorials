package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 接口。 */
@RestController
public class RemoteFsAgentController {

    private final RemoteFsAgent agent;

    public RemoteFsAgentController(RemoteFsAgent agent) {
        this.agent = agent;
    }

    @GetMapping("/workspace/remote")
    public String ask(
            @RequestParam(value = "message", defaultValue = "远程文件系统：通过抽象层访问远端文件") String message) {
        return agent.chat(message);
    }
}