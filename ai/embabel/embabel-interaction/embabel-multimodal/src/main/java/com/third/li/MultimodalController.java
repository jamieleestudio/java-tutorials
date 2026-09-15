package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多模态接口。
 *
 * <p>{@code imagePath} 省略时使用启动时自动生成的示例图片。
 * 需要先启动 docker 组件（LiteLLM + Ollama）并拉取 qwen2.5vl:3b，见模块 README。
 */
@RestController
public class MultimodalController {

    private final AgentPlatform agentPlatform;

    public MultimodalController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/multimodal/describe")
    public Description describe(
            @RequestParam(value = "imagePath", required = false) String imagePath) {
        return AgentInvocation.create(agentPlatform, Description.class)
                .invoke(new UserInput(imagePath == null ? "" : imagePath));
    }
}
