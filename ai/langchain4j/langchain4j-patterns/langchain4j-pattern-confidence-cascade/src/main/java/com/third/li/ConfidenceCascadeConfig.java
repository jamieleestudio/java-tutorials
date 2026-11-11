package com.third.li;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.decision.Decide;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Confidence Cascade（置信度级联）—— LangChain4j 1.23 新增的 agentic 模式。
 *
 * <p>思想：便宜模型先草答，**决策模型**评估置信度；置信度达到阈值就直接交付，
 * 否则升级到贵模型重答。用 10% 的成本覆盖 90% 的简单请求。
 *
 * <p>实现（1.23 决策智能体 + 条件升级）：
 * <ol>
 *   <li>{@code DraftAgent}（deepseek-chat）产出草稿，写入 state.draft</li>
 *   <li>{@code ConfidenceGate}：{@code decisionAgentBuilder} 声明的**决策智能体**，
 *       用决策模型给草稿打分，结果（boolean）写入 state.confident；
 *       概率低于 {@code threshold(0.6)} 即为 false</li>
 *   <li>conditionalBuilder：confident=false 时才触发 {@code DeepAgent}（deepseek-reasoner）重答</li>
 * </ol>
 */
@Configuration
public class ConfidenceCascadeConfig {

    public interface DraftAgent {

        @UserMessage("用不超过 3 句话快速回答用户问题，宁缺毋滥：{{request}}")
        @Agent("Drafts a quick answer with the cheap model")
        String draft(@V("request") String request);
    }

    public interface ConfidenceGate {

        @Decide("这份草稿能作为最终答案直接交付吗？是=准确完整可交付；否=有遗漏、错误或不够深入")
        @Agent("Judges whether the draft is confident enough")
        boolean confident(@V("draft") String draft);
    }

    public interface DeepAgent {

        @UserMessage("请深入、完整、结构化地回答用户问题：{{request}}")
        @Agent("Produces a deep answer with the expensive model")
        String deep(@V("request") String request);
    }

    @Bean
    public ChatModel cheapChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-chat")
                .build();
    }

    @Bean
    public ChatModel expensiveChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-reasoner")
                .build();
    }

    @Bean
    public DecisionModel confidenceDecisionModel(ChatModel cheapChatModel) {
        return new YesNoConfidenceDecisionModel(cheapChatModel);
    }

    @Bean
    public UntypedAgent confidenceCascade(ChatModel cheapChatModel, ChatModel expensiveChatModel,
                                          DecisionModel confidenceDecisionModel) {
        var draft = AgenticServices.agentBuilder(DraftAgent.class)
                .chatModel(cheapChatModel)
                .outputKey("draft")
                .build();

        var gate = AgenticServices.decisionAgentBuilder(ConfidenceGate.class)
                .decisionModel(confidenceDecisionModel)
                .threshold(0.6)
                .outputKey("confident")
                .build();

        var deep = AgenticServices.agentBuilder(DeepAgent.class)
                .chatModel(expensiveChatModel)
                .outputKey("answer")
                .build();

        var escalate = AgenticServices.conditionalBuilder()
                .subAgents(scope -> !scope.readState("confident", true), deep)
                .build();

        return AgenticServices.sequenceBuilder()
                .subAgents(draft, gate, escalate)
                .outputKey("answer")
                .build();
    }
}
