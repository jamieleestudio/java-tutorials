package com.third.li;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 结构化输出配置：接口方法直接返回 POJO，LangChain4j 自动 JSON 解析。 */
@Configuration
public class StructuredOutputConfig {

    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-chat")
                .temperature(0.0)
                .build();
    }

    @Bean
    public MovieExtractor movieExtractor(ChatModel chatModel) {
        return AiServices.builder(MovieExtractor.class)
                .chatModel(chatModel)
                .build();
    }
}
