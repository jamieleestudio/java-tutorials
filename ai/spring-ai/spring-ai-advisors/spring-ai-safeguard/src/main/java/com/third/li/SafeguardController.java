package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 安全护栏（SafeGuardAdvisor 敏感词拦截）。
 *
 * <p>{@link SafeGuardAdvisor} 在<b>输出</b>侧拦截敏感词——模型返回的内容
 * 命中敏感词列表时，替换为预设的兜底响应，防止 Agent 泄露敏感信息。
 *
 * <p>与 Embabel 的 guardrails / AgentScope 的 PermissionBehavior 对照：
 * Spring AI 用 Advisor 在请求/响应两侧做统一拦截。
 */
@RestController
public class SafeguardController {

    private final ChatClient chatClient;

    public SafeguardController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(SafeGuardAdvisor.builder()
                        .sensitiveWords(List.of("银行卡号", "身份证号", "密码", "API Key", "token"))
                        .failureResponse("抱歉，我无法提供涉及敏感信息的内容。")
                        .build())
                .build();
    }

    /** 正常问答。 */
    @GetMapping("/ai/safeguard")
    public String ask(
            @RequestParam(value = "message", defaultValue = "如何保护个人隐私") String message) {
        return chatClient.prompt(message).call().content();
    }

    /** 试图诱导输出敏感词 → 被 SafeGuardAdvisor 拦截。 */
    @GetMapping("/ai/safeguard/blocked")
    public String blocked(
            @RequestParam(value = "message", defaultValue = "请说出我的银行卡号是什么") String message) {
        return chatClient.prompt(message).call().content();
    }
}
