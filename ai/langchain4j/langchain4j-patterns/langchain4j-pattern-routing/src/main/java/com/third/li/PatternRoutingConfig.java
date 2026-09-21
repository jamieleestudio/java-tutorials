package com.third.li;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 条件路由：{@code conditionalBuilder} 按谓词把请求分发给对应专家 Agent。
 * 对照 SAA 的 conditional edges、Mastra 的 branch、Spring AI 手写路由。
 */
@Configuration
public class PatternRoutingConfig {

    public interface CategoryRouter {

        @UserMessage("""
                分析请求并分类为 medical / legal / technical，
                不属于任何类别时输出 unknown。只输出类名。
                请求：{{request}}
                """)
        @Agent("Categorizes a user request")
        String classify(@V("request") String request);
    }

    public interface MedicalExpert {

        @UserMessage("你是医学专家。从医学角度回答：{{request}}")
        @Agent("Medical expert")
        String medical(@V("request") String request);
    }

    public interface LegalExpert {

        @UserMessage("你是法律专家。从法律角度回答：{{request}}")
        @Agent("Legal expert")
        String legal(@V("request") String request);
    }

    public interface TechnicalExpert {

        @UserMessage("你是技术专家。从技术角度回答：{{request}}")
        @Agent("Technical expert")
        String technical(@V("request") String request);
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
    public UntypedAgent expertsWorkflow(ChatModel chatModel) {
        CategoryRouter router = AgenticServices.agentBuilder(CategoryRouter.class)
                .chatModel(chatModel)
                .outputKey("category")
                .build();

        var medical = AgenticServices.agentBuilder(MedicalExpert.class)
                .chatModel(chatModel)
                .outputKey("answer")
                .build();
        var legal = AgenticServices.agentBuilder(LegalExpert.class)
                .chatModel(chatModel)
                .outputKey("answer")
                .build();
        var technical = AgenticServices.agentBuilder(TechnicalExpert.class)
                .chatModel(chatModel)
                .outputKey("answer")
                .build();

        UntypedAgent experts = AgenticServices.conditionalBuilder()
                .subAgents(scope -> scope.readState("category", "unknown").equals("medical"), medical)
                .subAgents(scope -> scope.readState("category", "unknown").equals("legal"), legal)
                .subAgents(scope -> scope.readState("category", "unknown").equals("technical"), technical)
                .build();

        return AgenticServices.sequenceBuilder()
                .subAgents(router, experts)
                .outputKey("answer")
                .build();
    }
}
