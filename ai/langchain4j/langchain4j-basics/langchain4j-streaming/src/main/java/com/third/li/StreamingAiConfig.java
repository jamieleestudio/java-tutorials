package com.third.li;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 流式 AI 服务：接口方法返回 TokenStream，配合 SSE 下发。 */
@Configuration
public class StreamingAiConfig {

    public interface StreamingWriter {

        @UserMessage("写一首关于{{topic}}的五言绝句")
        TokenStream write(@V("topic") String topic);
    }

    @Bean
    public StreamingWriter streamingWriter(ChatModel chatModel, StreamingChatModel streamingChatModel) {
        return AiServices.builder(StreamingWriter.class)
                .streamingChatModel(streamingChatModel)
                .chatModel(chatModel)
                .build();
    }
}
