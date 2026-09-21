package com.third.li;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文本分类：AiServices 方法直接返回枚举 —— LangChain4j 自动把模型输出
 * 映射为枚举值（结构化输出的特殊形式），对照 SAA 的结构化输出、
 * Mastra 的 structuredOutput。
 */
@Configuration
public class ClassificationConfig {

    public enum Sentiment {
        POSITIVE, NEGATIVE, NEUTRAL
    }

    public interface SentimentAnalyzer {

        @UserMessage("分析下面评论的情感倾向，只返回 POSITIVE / NEGATIVE / NEUTRAL 之一：\n{{text}}")
        Sentiment analyze(@V("text") String text);
    }

    @Bean
    public SentimentAnalyzer sentimentAnalyzer(ChatModel chatModel) {
        return AiServices.builder(SentimentAnalyzer.class)
                .chatModel(chatModel)
                .build();
    }
}
