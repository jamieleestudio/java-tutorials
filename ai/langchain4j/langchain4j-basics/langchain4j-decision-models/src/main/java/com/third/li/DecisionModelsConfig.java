package com.third.li;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.router.ChatModelRoute;
import dev.langchain4j.model.chat.router.ChatModelRoutingRequest;
import dev.langchain4j.model.chat.router.ChatModelRoutingResult;
import dev.langchain4j.model.chat.router.DecisionModelChatModelRouter;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.decision.DecisionServices;
import dev.langchain4j.service.decision.Decide;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 决策模型三件套（LangChain4j 1.21+，@Experimental）：
 * <ol>
 *   <li>{@link DecisionServices}：像 AI Services 一样用接口 + {@code @Decide} 声明决策问题，
 *       返回 boolean / 枚举，框架负责问题构建与答案映射</li>
 *   <li>{@link ChatModelDecisionModel}：本模块自定义的决策模型适配器（DeepSeek 可用）</li>
 *   <li>{@link DecisionModelChatModelRouter}（1.22+）：用决策模型把请求路由到不同的 ChatModel</li>
 * </ol>
 *
 * <p>决策模型适合分类/路由/门控/打分：一次调用拿回类型化答案 + 概率，
 * 比让 ChatModel 生成文本再解析更便宜、更稳定。
 */
@Configuration
public class DecisionModelsConfig {

    public enum Team {
        BILLING, TECHNICAL, GENERAL
    }

    /** 用 @Decide 声明决策问题的接口 —— DecisionServices.create 生成实现。 */
    public interface TicketDecisions {

        @Decide("这条用户消息是投诉吗？是=表达强烈不满并要求处理")
        boolean isComplaint(String message);

        @Decide("这条消息应该交给哪个团队？BILLING=账单/退款，TECHNICAL=故障/报错，GENERAL=其它咨询")
        Team team(String message);
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
    public DecisionModel decisionModel(ChatModel chatModel) {
        return new ChatModelDecisionModel(chatModel);
    }

    @Bean
    public TicketDecisions ticketDecisions(DecisionModel decisionModel) {
        return DecisionServices.create(TicketDecisions.class, decisionModel);
    }

    /** 决策模型路由：按问题把请求分给 simple / complex 两类模型。 */
    @Bean
    public DecisionModelChatModelRouter decisionRouter(DecisionModel decisionModel) {
        return DecisionModelChatModelRouter.builder()
                .decisionModel(decisionModel)
                .question("这个请求应该由哪类模型处理？simple=闲聊/简单问答，complex=复杂推理/多步骤分析")
                .minProbability(0.5)
                .build();
    }

    @Bean
    public List<ChatModelRoute> supportRoutes() {
        return List.of(
                new ChatModelRoute("simple", List.of("闲聊、简单问答、翻译")),
                new ChatModelRoute("complex", List.of("复杂推理、多步骤分析、代码审查")));
    }

    /** 演示用：路由结果只选名字，真正发请求时按名字选模型。 */
    public ChatModelRoutingResult route(ChatModelChatModelRoutingSupport support, String message) {
        ChatModelRoutingRequest request = ChatModelRoutingRequest.builder()
                .chatRequest(ChatRequest.builder().messages(UserMessage.from(message)).build())
                .routes(support.routes())
                .build();
        return support.router().route(request);
    }

    /** 聚合 router 与 routes，避免 Controller 直接依赖两个 Bean。 */
    public record ChatModelChatModelRoutingSupport(DecisionModelChatModelRouter router, List<ChatModelRoute> routes) {
    }

    @Bean
    public ChatModelChatModelRoutingSupport routingSupport(DecisionModelChatModelRouter router,
                                                           List<ChatModelRoute> routes) {
        return new ChatModelChatModelRoutingSupport(router, routes);
    }

    /** 便宜的 simple 模型（演示用，同一端点不同模型名）。 */
    @Bean
    public OpenAiChatModel simpleChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-chat")
                .build();
    }

    /** 贵的 complex 模型。 */
    @Bean
    public OpenAiChatModel complexChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-reasoner")
                .build();
    }
}
