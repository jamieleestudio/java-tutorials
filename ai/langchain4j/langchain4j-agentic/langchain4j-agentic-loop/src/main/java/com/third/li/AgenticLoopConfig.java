package com.third.li;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 循环工作流：{@code loopBuilder} + {@code exitCondition} 打分迭代 ——
 * 评分 Agent 打分，不达标则改写 Agent 迭代，直到分数 ≥ 0.8 或 5 轮封顶。
 */
@Configuration
public class AgenticLoopConfig {

    public interface StyleScorer {

        @UserMessage("""
                你是严格的审稿人。给下面的故事打一个 0.0 到 1.0 的分，
                依据是它与 {{style}} 风格的契合度。只输出分数。
                故事："{{story}}"
                """)
        @Agent("Scores a story based on style alignment")
        double scoreStyle(@V("story") String story, @V("style") String style);
    }

    public interface StyleEditor {

        @UserMessage("""
                你是专业编辑。把下面的故事改写得更符合 {{style}} 风格，只输出改写后的故事。
                故事："{{story}}"
                """)
        @Agent("Edits a story for style")
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
    public UntypedAgent styleReviewLoop(ChatModel chatModel) {
        StyleEditor styleEditor = AgenticServices.agentBuilder(StyleEditor.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        StyleScorer styleScorer = AgenticServices.agentBuilder(StyleScorer.class)
                .chatModel(chatModel)
                .outputKey("score")
                .build();

        return AgenticServices.loopBuilder()
                .subAgents(styleScorer, styleEditor)
                .maxIterations(5)
                .exitCondition(agenticScope -> agenticScope.readState("score", 0.0) >= 0.8)
                .build();
    }
}
