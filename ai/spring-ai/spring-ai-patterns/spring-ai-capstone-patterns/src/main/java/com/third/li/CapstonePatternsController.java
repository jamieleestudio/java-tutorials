package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Patterns Capstone 模式（模式综合）。
 *
 * <p>把多个模式/组件整合进一个 ChatClient：
 * <ul>
 *   <li><b>记忆</b>：MessageChatMemoryAdvisor 多轮上下文</li>
 *   <li><b>安全</b>：SafeGuardAdvisor 敏感词拦截</li>
 *   <li><b>工具</b>：@Tool 订单查询（函数调用）</li>
 *   <li><b>观测</b>：SimpleLoggerAdvisor 日志</li>
 * </ul>
 *
 * <p>与 Embabel embabel-capstone / AgentScope agentscope-capstone-patterns 对照。
 */
@RestController
public class CapstonePatternsController {

    private final ChatClient chatClient;

    public CapstonePatternsController(ChatClient.Builder chatClientBuilder) {
        var memory = MessageWindowChatMemory.builder().maxMessages(10).build();
        this.chatClient = chatClientBuilder
                .defaultSystem("你是智能客服助手。查询订单时调用 getOrderStatus 工具。")
                .defaultTools(new OrderService())
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(memory).build(),
                        SafeGuardAdvisor.builder()
                                .sensitiveWords(List.of("银行卡号", "密码", "token"))
                                .failureResponse("抱歉，无法提供敏感信息。")
                                .build())
                .build();
    }

    /** 综合客服：记忆 + 工具 + 安全 + 日志。 */
    @GetMapping("/ai/capstone")
    public String chat(
            @RequestParam(value = "message", defaultValue = "帮我查一下订单 A1001 的状态") String message,
            @RequestParam(value = "chatId", defaultValue = "cs-1") String chatId) {
        return chatClient.prompt(message)
                .advisors(a -> a.param("chatId", chatId))
                .call().content();
    }

    /** 订单服务工具。 */
    public static class OrderService {

        @Tool(name = "getOrderStatus", description = "查询订单的当前状态")
        public String getOrderStatus(@ToolParam(description = "订单号") String orderId) {
            return "订单 " + orderId + " 状态：已发货，预计 2 天内送达";
        }
    }
}
