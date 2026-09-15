package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 语义检索接口。
 *
 * <p>需要先启动 docker 组件（LiteLLM + Ollama），见模块 README。
 */
@RestController
public class EmbeddingsController {

    private final AgentPlatform agentPlatform;

    public EmbeddingsController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/embeddings/search")
    public SearchResult search(
            @RequestParam(value = "message", defaultValue = "怎么控制大知识库的 token 成本？") String message) {
        return AgentInvocation.create(agentPlatform, SearchResult.class).invoke(new UserInput(message));
    }
}
