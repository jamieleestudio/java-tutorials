package com.third.li;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AiServices 声明式配置。
 *
 * <p>{@code AiServices.builder(接口).chatModel(模型).build()} 把普通 Java 接口
 * 变成 AI 服务：接口方法上的 {@code @SystemMessage} / {@code @UserMessage} 注解
 * 即提示词模板，{@code @V} 绑定模板变量 —— 与 embabel 的 @Agent、
 * SAA 的 AiServices 同一"声明式"范式（LangChain4j 是这一范式的 Java 先行者）。
 */
@Configuration
public class AiServicesConfig {

    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-chat")
                .temperature(0.7)
                .build();
    }

    @Bean
    public Assistant assistant(ChatModel chatModel) {
        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .build();
    }
}
