package com.third.li;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agentic 基础：{@code @Agent} 注解声明 Agent 接口，{@code AgenticServices.sequenceBuilder()}
 * 把多个 Agent 串成顺序工作流。{@code AgenticScope} 是 Agent 间共享状态容器
 * （outputKey 写入，@V 参数读取）—— 对照 SAA 的 Graph 状态、Mastra 的 workflow。
 */
@Configuration
public class AgenticBasicsConfig {

    public interface CreativeWriter {

        @UserMessage("""
                你是创意写手。围绕主题 {{topic}} 写一个不超过 3 句的故事草稿，只输出故事。
                """)
        @Agent("Generates a story based on the given topic")
        String generateStory(@V("topic") String topic);
    }

    public interface StyleEditor {

        @UserMessage("""
                你是专业编辑。把下面的故事改写成 {{style}} 风格，只输出改写后的故事。
                故事："{{story}}"
                """)
        @Agent("Edits a story to better fit a given style")
        String editStory(@V("story") String story, @V("style") String style);
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
    public UntypedAgent storyWorkflow(ChatModel chatModel) {
        CreativeWriter creativeWriter = AgenticServices.agentBuilder(CreativeWriter.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        StyleEditor styleEditor = AgenticServices.agentBuilder(StyleEditor.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        // 顺序工作流：写故事 → 改风格；输出经 AgenticScope 的 story 变量传递
        UntypedAgent novelCreator = AgenticServices.sequenceBuilder()
                .subAgents(creativeWriter, styleEditor)
                .outputKey("story")
                .build();

        return novelCreator;
    }
}
