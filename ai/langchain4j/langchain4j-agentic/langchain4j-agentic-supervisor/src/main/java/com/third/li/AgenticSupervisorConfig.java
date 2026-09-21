package com.third.li;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 主管模式：{@code supervisorBuilder} 由主管 LLM 决策把任务委派给哪个专家 Agent
 * （专家以工具形式暴露给主管），执行完由主管汇总 —— 与 SAA 的 supervisorBuilder、
 * Mastra 的 supervisor 模式同构。
 *
 * <p>场景：银行转账主管 —— 根据请求依次委派"扣款专家"与"汇率换算专家"。
 */
@Configuration
public class AgenticSupervisorConfig {

    public interface WithdrawAgent {

        @UserMessage("从 {{account}} 账户取出 {{amount}} 元并存入 Georgios 账户。")
        @Agent("Withdraws the given amount from an account and deposits it to Georgios")
        String withdraw(@V("account") String account, @V("amount") double amount);
    }

    public interface ExchangeAgent {

        @UserMessage("把 {{amount}} 元人民币按 1:18 汇率换算成日元，只输出换算结果。")
        @Agent("Converts CNY to JPY")
        String exchange(@V("amount") double amount);
    }

    public static class BankTool {

        private final java.util.Map<String, Double> accounts = new java.util.HashMap<>();

        @Tool("给指定账户存入金额")
        void credit(@P("账户名") String account, @P("金额") double amount) {
            accounts.merge(account, amount, Double::sum);
        }

        @Tool("从指定账户扣除金额")
        void debit(@P("账户名") String account, @P("金额") double amount) {
            accounts.computeIfPresent(account, (k, v) -> v - amount);
        }

        @Tool("查询指定账户余额")
        double balance(@P("账户名") String account) {
            return accounts.getOrDefault(account, 0.0);
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
    public BankTool bankTool() {
        BankTool tool = new BankTool();
        tool.credit("Mario", 1000.0);
        tool.credit("Georgios", 1000.0);
        return tool;
    }

    @Bean
    public SupervisorAgent bankSupervisor(ChatModel chatModel, BankTool bankTool) {
        WithdrawAgent withdrawAgent = AgenticServices.agentBuilder(WithdrawAgent.class)
                .chatModel(chatModel)
                .tools(bankTool)
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
