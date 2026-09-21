package com.third.li;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 模型配置：OpenAI 兼容协议接入 DeepSeek。
 *
 * <p>langchain4j-open-ai 的 {@code OpenAiChatModel} 通过 baseUrl 指向任意
 * OpenAI 兼容端点 —— 与 Java 侧其他组的 DeepSeek 接入方式一致，
 * API key 从环境变量 DEEPSEEK_API_KEY 读取。
 */
@Configuration
public class ChatModelConfig {

    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-chat")
                .temperature(0.7)
                .build();
    }
}
