package com.third.li;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多用户隔离：{@code ChatMemoryProvider} 按 memoryId 为每个用户提供独立记忆，
 * 接口方法用 {@code @MemoryId} 声明归属 —— 与 SAA 的 memoryId、Mastra 的 thread 同构。
 */
@Configuration
public class MultiUserConfig {

    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-chat")
                .build();
    }

    @Bean
    public UserAssistant userAssistant(ChatModel chatModel) {
        return AiServices.builder(UserAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}
