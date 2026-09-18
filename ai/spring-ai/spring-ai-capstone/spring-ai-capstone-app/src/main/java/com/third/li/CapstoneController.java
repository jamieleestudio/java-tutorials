package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 端到端综合（客服 Agent 全栈）。
 *
 * <p>整合教程全部能力，构建一个可用的"企业知识问答客服"：
 * <ul>
 *   <li><b>RAG</b>：VectorStore + 检索知识库回答产品问题</li>
 *   <li><b>记忆</b>：MessageChatMemoryAdvisor 多轮上下文</li>
 *   <li><b>工具</b>：@Tool 查询订单/库存</li>
 *   <li><b>安全</b>：SafeGuardAdvisor 敏感词拦截</li>
 * </ul>
 */
@RestController
public class CapstoneController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public CapstoneController(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
        // 内存知识库：产品 FAQ
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(List.of(
                new Document("我们的产品支持免费试用 14 天，无需绑定银行卡。", Map.of("topic", "trial")),
                new Document("企业版支持 SSO 单点登录和审计日志。", Map.of("topic", "enterprise")),
                new Document("退款政策：购买后 30 天内可全额退款。", Map.of("topic", "refund"))));

        var memory = MessageWindowChatMemory.builder().maxMessages(12).build();
        this.chatClient = chatClientBuilder
                .defaultSystem("你是企业客服助手。结合知识库回答产品问题；查订单/库存时调用工具。")
                .defaultTools(new OrderTools())
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(memory).build(),
                        SafeGuardAdvisor.builder()
                                .sensitiveWords(List.of("银行卡号", "身份证号", "密码"))
                                .failureResponse("抱歉，我无法提供涉及敏感信息的答复。")
                                .build())
                .build();
    }

    /** 客服问答（RAG + 记忆 + 工具 + 安全）。 */
    @GetMapping("/ai/capstone")
    public String chat(
            @RequestParam(value = "message", defaultValue = "你们的退款政策是什么") String message,
            @RequestParam(value = "chatId", defaultValue = "customer-1") String chatId) {
        // RAG：检索知识库
        List<Document> docs = vectorStore.similaritySearch(message);
        String context = docs.isEmpty() ? "(无相关知识)" :
                docs.stream().map(Document::getText).reduce("", (a, b) -> a + "\n- " + b);

        return chatClient.prompt()
                .system("相关产品知识：\n" + context)
                .user(message)
                .advisors(a -> a.param("chatId", chatId))
                .call().content();
    }

    /** 订单工具。 */
    public static class OrderTools {

        @Tool(name = "getOrderStatus", description = "查询订单状态")
        public String getOrderStatus(@ToolParam(description = "订单号") String orderId) {
            return "订单 " + orderId + "：已支付，正在打包发货";
        }

        @Tool(name = "checkStock", description = "查询商品库存")
        public String checkStock(@ToolParam(description = "商品名") String product) {
            return product + " 当前库存：充足（1200 件）";
        }
    }
}
