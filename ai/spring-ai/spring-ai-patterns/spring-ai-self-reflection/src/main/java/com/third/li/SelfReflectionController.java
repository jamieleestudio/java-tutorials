package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Self-Reflection 模式（自省 + 渐进式工具）。
 *
 * <p>用自定义 Advisor 在<b>每次调用后</b>注入反思提示（"检查你的回答是否
 * 正确完整，如有不足请补充"），让模型在后续轮次自我修正。
 *
 * <p>与 Embabel embabel-tools-advanced / AgentScope agentscope-trigger 的自省
 * 思路对照——Spring AI 用 Advisor 的 after 阶段做反思注入。
 */
@RestController
public class SelfReflectionController {

    private final ChatClient chatClient;

    public SelfReflectionController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new ReflectionAdvisor())
                .build();
    }

    /** 带自省 Advisor 的调用。 */
    @GetMapping("/ai/self-reflection")
    public String ask(
            @RequestParam(value = "message", defaultValue = "列出 Java 中 5 种创建线程的方式") String message) {
        return chatClient.prompt(message).call().content();
    }

    /**
     * 自省 Advisor：after 阶段提示模型反思并补充。
     * （简化演示：实际多轮反思需要配合工具循环。）
     */
    public static class ReflectionAdvisor implements BaseAdvisor {

        @Override
        public int getOrder() {
            return 2;
        }

        @Override
        public String getName() {
            return "ReflectionAdvisor";
        }

        @Override
        public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
            // 在系统提示后追加自省要求
            return request.mutate()
                    .prompt(new org.springframework.ai.chat.prompt.Prompt(
                            request.prompt().getInstructions(),
                            request.prompt().getOptions()))
                    .build();
        }

        @Override
        public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
            // 可在此追加反思逻辑（教学演示打印日志）
            System.out.println("[ReflectionAdvisor] 模型返回长度：" +
                    (response.chatResponse().getResult().getOutput().getText() == null
                            ? 0 : response.chatResponse().getResult().getOutput().getText().length()));
            return response;
        }
    }
}
