package com.third.li;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.exception.LlmVisibleToolExecutionException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具错误处理（LangChain4j 1.21+ 新特性）：
 * **让每个异常自己决定向 LLM 暴露什么**。
 *
 * <ul>
 *   <li>抛出 {@link LlmVisibleToolExecutionException}：消息会原样给到 LLM，
 *       模型可以据此向用户解释、换路重试 —— 适合"预期内的业务失败"</li>
 *   <li>抛出普通 RuntimeException：默认不会进入 LLM 上下文，只有通用失败提示 ——
 *       避免把堆栈、内网地址、敏感信息泄露给模型</li>
 * </ul>
 *
 * <p>对照 1.21 之前的做法：全局只有一个 ToolExecutionExceptionProcessor 开关，
 * 要么全暴露要么全隐藏；现在粒度细化到"每个异常"。
 */
@Configuration
public class ToolErrorsConfig {

    public interface OpsAssistant {

        @SystemMessage("你是运维助手。工具失败时，根据工具给出的错误信息向用户解释并给出建议。")
        String chat(@UserMessage String message);
    }

    static class FragileTools {

        @Tool(name = "queryStock", value = "查询指定 SKU 的实时库存")
        public String queryStock(@P("SKU 编码") String sku) {
            // 预期内的业务失败：把"对 LLM 友好"的信息显式抛出
            throw new LlmVisibleToolExecutionException(
                    "库存服务暂时超时，无法查询 SKU-" + sku
                            + " 的实时库存。可建议用户 5 分钟后重试，或改用最近一次快照数据。");
        }

        @Tool(name = "writeAuditLog", value = "写一条审计日志")
        public String writeAuditLog(@P("操作内容") String action) {
            // 非预期异常：消息（含内网细节）默认不进入 LLM 上下文
            throw new IllegalStateException("db connection refused at 10.0.0.7:5432, password=***");
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
    public OpsAssistant opsAssistant(ChatModel chatModel) {
        return AiServices.builder(OpsAssistant.class)
                .chatModel(chatModel)
                .tools(new FragileTools())
                .build();
    }
}
