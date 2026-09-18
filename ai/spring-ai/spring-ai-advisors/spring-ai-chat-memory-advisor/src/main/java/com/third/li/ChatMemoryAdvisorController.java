package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话记忆（MessageChatMemoryAdvisor + MessageWindowChatMemory）。
 *
 * <p>Spring AI 的记忆分两层：
 * <ul>
 *   <li>{@link MessageWindowChatMemory} — 存储：保留最近 N 条消息</li>
 *   <li>{@link MessageChatMemoryAdvisor} — 拦截：before 注入历史，after 记录新消息</li>
 * </ul>
 *
 * <p>通过 ChatClient 的 {@code .advisors(a -> a.param("chatId", ...))} 区分会话。
 * 与 AgentScope 的 ChatMemory / Embabel 的 Process 状态对照。
 */
@RestController
public class ChatMemoryAdvisorController {

    private final ChatClient chatClient;

    public ChatMemoryAdvisorController(ChatClient.Builder chatClientBuilder) {
        var memory = MessageWindowChatMemory.builder()
                .maxMessages(10)   // 最多记住 10 条
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();
    }

    /** 带记忆的多轮对话：同一 chatId 记住上下文。 */
    @GetMapping("/ai/memory")
    public String chat(
            @RequestParam(value = "message", defaultValue = "你好，我叫小明") String message,
            @RequestParam(value = "chatId", defaultValue = "session-1") String chatId) {
        return chatClient.prompt(message)
                .advisors(a -> a.param("chatId", chatId))
                .call()
                .content();
    }
}
