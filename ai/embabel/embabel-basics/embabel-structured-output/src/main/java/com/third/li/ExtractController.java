package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 结构化输出接口：直接返回强类型对象，由 Spring 序列化为 JSON。
 */
@RestController
public class ExtractController {

    private final AgentPlatform agentPlatform;

    public ExtractController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/extract")
    public Profile extract(
            @RequestParam(value = "text",
                    defaultValue = "你好，我是李四，今年 28 岁，平时主要用 Java 和 Kubernetes，也写一点 Python，"
                            + "目前专注在云原生和后端架构方向。") String text) {
        return AgentInvocation.create(agentPlatform, Profile.class).invoke(new UserInput(text));
    }
}
