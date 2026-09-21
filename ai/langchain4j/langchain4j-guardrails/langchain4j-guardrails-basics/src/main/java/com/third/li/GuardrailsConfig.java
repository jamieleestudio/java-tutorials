package com.third.li;

import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 输入护栏：{@code InputGuardrail} 在请求到达模型前校验/改写用户输入。
 * 本例拦截包含手机号的输入，把号码打码后再交给模型。
 */
@Configuration
public class GuardrailsConfig {

    /** 简单手机号打码护栏（生产可用正则库/PII 检测器）。 */
    static class PhoneMaskGuardrail implements InputGuardrail {

        @Override
        public InputGuardrailResult validate(InputGuardrailRequest request) {
            String text = request.userMessage().singleText();
            String masked = text.replaceAll("(1[3-9]\\d)\\d{4}(\\d{4})", "$1****$2");
            if (!masked.equals(text)) {
                return successWith(masked);
            }
            return success();
        }
    }

    public interface SafeAssistant {

        String chat(String message);
    }

    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-chat")
                .build();
    }

    @Bean
    public SafeAssistant safeAssistant(ChatModel chatModel) {
        return AiServices.builder(SafeAssistant.class)
                .chatModel(chatModel)
                .inputGuardrailClasses(PhoneMaskGuardrail.class)
                .build();
    }
}
