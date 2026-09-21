package com.third.li;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 主管模式（对照 agentic-supervisor 的独立演示，patterns 侧提供完整业务编排）：
 * 主管根据请求在 withdraw / exchange 专家间决策并汇总。
 */
@Configuration
public class PatternSupervisorConfig {

    public interface WithdrawAgent {

        @UserMessage("从 {{account}} 账户取出 {{amount}} 元并存入 Georgios 账户。")
        @Agent("Withdraws the given amount")
        String withdraw(@V("account") String account, @V("amount") double amount);
    }

    public interface ExchangeAgent {

        @UserMessage("把 {{amount}} 元人民币按 1:18 汇率换算成日元，只输出结果。")
        @Agent("Converts CNY to JPY")
        String exchange(@V("amount") double amount);
    }

    public static class BankTools {

        private final java.util.Map<String, Double> accounts = new java.util.HashMap<>();

        @Tool("给指定账户存入金额")
        void credit(@P("账户名") String account, @P("金额") double amount) {
            accounts.merge(account, amount, Double::sum);
        }

        @Tool("从指定账户扣除金额")
        void debit(@P("账户名") String account, @P("金额") double amount) {
            accounts.computeIfPresent(account, (k, v) -> v - amount);
        }

        @Bean
        public java.util.Map<String, Double> snapshot() {
            return java.util.Collections.unmodifiableMap(accounts);
        }
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
    public SupervisorAgent bankSupervisor(ChatModel chatModel) {
        BankTools bankTools = new BankTools();
        bankTools.credit("Mario", 1000.0);
        bankTools.credit("Georgios", 1000.0);

        WithdrawAgent withdrawAgent = AgenticServices.agentBuilder(WithdrawAgent.class)
                .chatModel(chatModel)
                .tools(bankTools)
                .outputKey("withdraw_result")
                .build();

        ExchangeAgent exchangeAgent = AgenticServices.agentBuilder(ExchangeAgent.class)
                .chatModel(chatModel)
                .outputKey("exchange_result")
                .build();

        return AgenticServices.supervisorBuilder()
                .chatModel(chatModel)
                .subAgents(withdrawAgent, exchangeAgent)
                .build();
    }
}
