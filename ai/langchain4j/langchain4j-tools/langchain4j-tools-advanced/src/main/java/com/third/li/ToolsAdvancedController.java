package com.third.li;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.tool.ToolExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 编程式工具注册：{@code ToolSpecification}（手工声明 schema）+
 * {@code ToolExecutor}（手工实现执行逻辑），以 {@code tools(Map)} 注册。
 *
 * <p>与注解式（{@code @Tool}）的差别：schema 与执行完全由代码控制，
 * 适合动态生成工具（从注册表/数据库读出工具定义再装配）。
 * 这里同时手工驱动一次"模型决策 → 工具执行 → 结果回填"的工具循环，
 * 展示 AiServices 注解模式在底层做的事情。
 */
@RestController
public class ToolsAdvancedController {

    private final ChatModel chatModel;

    public ToolsAdvancedController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    record ExchangeResult(double usd, String result) {
    }

    @GetMapping("/ai/tools/advanced")
    public ExchangeResult exchange() {
        // 1. 手工声明工具 schema（等价于 @Tool 注解生成的元数据）
        ToolSpecification spec = ToolSpecification.builder()
                .name("usdToCny")
                .description("把美元金额按固定汇率换算成人民币")
                .parameters(JsonObjectSchema.builder()
                        .addNumberProperty("usd", "美元金额")
                        .required("usd")
                        .build())
                .build();

        // 2. 手工实现工具执行器（演示固定汇率）
        ToolExecutor executor = (request, memoryId) -> "720.0";

        // 3. 编程式注册并请求模型
        Map<ToolSpecification, ToolExecutor> tools = Map.of(spec, executor);
        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from("把 100 美元换算成人民币是多少？请调用工具。"))
                .parameters(ChatRequestParameters.builder()
                        .toolSpecifications(spec)
                        .build())
                .build();
        ChatResponse response = chatModel.chat(request);

        // 4. 检查模型是否发起了工具调用（完整循环需要执行工具并把结果回填再请求一次）
        String toolInfo;
        if (response.aiMessage().hasToolExecutionRequests()) {
            ToolExecutionRequest req = response.aiMessage().toolExecutionRequests().get(0);
            toolInfo = "模型调用工具 usdToCny，参数 " + req.arguments() + "，执行结果 " + executor.execute(req, null);
        } else {
            toolInfo = "模型未调用工具，直接回答：" + response.aiMessage().text();
        }

        return new ExchangeResult(100, "720.0 元人民币（汇率 7.2）。" + toolInfo);
    }
}
