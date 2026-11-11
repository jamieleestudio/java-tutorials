package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 可配置工具调用上限（Spring AI 2.0.1 新特性）。
 *
 * <p>application.yml 中 {@code spring.ai.tools.limits.*} 全局控制：
 * <pre>
 * spring.ai.tools.limits:
 *   max-calls-per-tool-default: 2        # 每个工具最多被调用 2 次
 *   max-calls-per-tool: { getPrice: 3 }  # 按工具名覆盖
 *   excluded-tools: [safeTool]           # 不受限的工具
 *   max-total-tool-calls: 10             # 单次对话总调用上限
 *   on-limit-exceeded: RETURN_ERROR_RESPONSE  # 或 THROW
 * </pre>
 *
 * <p>超限行为（{@code ToolCallLimitBehavior}）：
 * <ul>
 *   <li>{@code RETURN_ERROR_RESPONSE}：给模型回一条错误工具结果，模型可继续组织回答（优雅降级）</li>
 *   <li>{@code THROW}：抛 {@code ToolCallLimitExceededException}，带 toolName/limit/部分结果，
 *       由应用侧决定怎么处理（{@code buildGeneration()} 还能拿到兜底 Generation）</li>
 * </ul>
 *
 * <p>演示：一个"查询股价"工具 + 诱导模型反复调用的提问，观察超限后的行为。
 */
@RestController
public class ToolLimitsController {

    private final ChatClient chatClient;

    public ToolLimitsController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultTools(new StockTools())
                .build();
    }

    /** 模型大概率对 5 只股票各调一次价格工具 → 触发 max-calls-per-tool-default=2 的限制。 */
    @GetMapping("/ai/tools/limits")
    public String limits(
            @RequestParam(value = "message",
                    defaultValue = "请分别查询 AAPL、MSFT、GOOG、AMZN、META 的股价，然后汇总对比") String message) {
        return chatClient.prompt(message).call().content();
    }

    /** 询问而不需要工具的场景（对照：不触发限制）。 */
    @GetMapping("/ai/tools/limits/noop")
    public String noop(
            @RequestParam(value = "message", defaultValue = "股价工具每只股票最多能被调用几次？") String message) {
        return chatClient.prompt(message).call().content();
    }

    public static class StockTools {

        @Tool(name = "getPrice", description = "查询一只股票的当前价格")
        public String getPrice(@ToolParam(description = "股票代码，如 AAPL") String symbol) {
            return symbol + " 当前价格：" + (100 + symbol.length() * 7) + ".00 USD（模拟）";
        }
    }
}
