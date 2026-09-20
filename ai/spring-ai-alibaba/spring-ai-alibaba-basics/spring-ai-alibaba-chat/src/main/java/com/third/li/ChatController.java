package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基础聊天（ChatClient + ChatModel + DeepSeek）。
 *
 * <p>Spring AI Alibaba 是 Spring AI 的超集：ChatClient / ChatModel 等核心抽象完全一致，
 * 差异在于 SAA 提供了 DashScope 模型接入、Graph 编排、Agent Framework 等扩展。
 * 本模块先用与 {@code ai/spring-ai/spring-ai-chat} 相同的方式跑通最小链路，
 * 作为后续 Graph / Agent 模块的基线对照。
 *
 * <p>自动配置：{@code spring-ai-starter-model-openai} + application.yml 里
 * base-url 指向 DeepSeek（OpenAI 兼容接口），自动装配 {@code ChatModel} Bean。
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final ChatModel chatModel;

    public ChatController(ChatClient.Builder chatClientBuilder, ChatModel chatModel) {
        this.chatClient = chatClientBuilder.build();
        this.chatModel = chatModel;
    }

    /** ChatClient 高层 API：同步调用。 */
    @GetMapping("/ai/chat")
    public String chat(
            @RequestParam(value = "message", defaultValue = "用一句话介绍 Spring AI Alibaba") String message) {
        return chatClient.prompt(message).call().content();
    }

    /** ChatModel 底层 API：call(Prompt) 返回完整 ChatResponse。 */
    @GetMapping("/ai/chat/model")
    public String chatModel(
            @RequestParam(value = "message", defaultValue = "用一句话介绍 Spring AI Alibaba") String message) {
        var response = chatModel.call(message);
        return "模型输出：" + response;
    }

    /** ChatClient 带系统提示：defaultSystem 注入角色。 */
    @GetMapping("/ai/chat/system")
    public String chatWithSystem(
            @RequestParam(value = "message", defaultValue = "介绍一下你自己") String message) {
        return chatClient.prompt()
                .system("你是一个精通 Java 和 AI 的架构师助手，回答简洁专业。")
                .user(message)
                .call()
                .content();
    }
}
