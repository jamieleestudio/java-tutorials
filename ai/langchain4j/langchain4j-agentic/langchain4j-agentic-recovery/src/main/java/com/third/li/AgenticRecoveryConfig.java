package com.third.li;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.agentic.agent.ErrorRecoveryResult;
import dev.langchain4j.agentic.agent.MissingArgumentException;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 错误恢复：{@code errorHandler} 把失败转化为重试。
 * 场景：首步必需参数缺失（MissingArgumentException），handler 检测后
 * 补写 AgenticScope 再 retry —— 对照 embabel 的 stuck-handler、agentscope 的 replanning。
 */
@Configuration
public class AgenticRecoveryConfig {

    public interface StoryWriter {

        @UserMessage("""
                你是创意写手。围绕主题 {{topic}} 写一个不超过 3 句的故事草稿，只输出故事。
                """)
        @Agent("Generates a story based on the given topic")
        String generateStory(@V("topic") String topic);
    }

    public interface AudienceEditor {

        @UserMessage("""
                你是专业编辑。把下面的故事改写为 {{audience}} 适合的版本，只输出改写后的故事。
                故事："{{story}}"
                """)
        @Agent("Edits a story for the target audience")
        String editStory(@V("story") String story, @V("audience") String audience);
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
    public UntypedAgent recoveryWorkflow(ChatModel chatModel) {
        StoryWriter writer = AgenticServices.agentBuilder(StoryWriter.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        AudienceEditor editor = AgenticServices.agentBuilder(AudienceEditor.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        return AgenticServices.sequenceBuilder()
                .subAgents(writer, editor)
                .errorHandler(errorContext -> {
                    // 演示：若首步缺 topic 参数，补一个默认值后重试
                    if (errorContext.exception() instanceof
                            MissingArgumentException mEx
                            && mEx.argumentName().equals("topic")) {
                        errorContext.agenticScope().writeState("topic", "默认主题：AI 与 Java");
                        return ErrorRecoveryResult.retry();
                    }
                    return ErrorRecoveryResult.throwException();
                })
                .outputKey("story")
                .build();
    }
}
