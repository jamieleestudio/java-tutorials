package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 函数调用（@Tool + ChatClient.tools()）。
 *
 * <p>Spring AI 2.0 用 {@link Tool} 注解标记方法为工具，ChatClient 自动生成
 * JSON Schema 并让模型自主调用。2.0 中工具调用由 {@code ToolCallingAdvisor}
 * 处理（Advisor 链的一环）。
 *
 * <p>用法：{@code chatClient.prompt(msg).tools(myBean).call()}，
 * myBean 是被扫描 {@code @Tool} 方法的对象。
 */
@RestController
public class FunctionCallingController {

    private final ChatClient chatClient;

    public FunctionCallingController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultTools(new WeatherTools())
                .build();
    }

    /** 模型自主调用天气工具。 */
    @GetMapping("/ai/function")
    public String function(
            @RequestParam(value = "message", defaultValue = "北京和上海的天气怎么样") String message) {
        return chatClient.prompt(message).call().content();
    }

    /** 指定工具调用（tools 按需传入）。 */
    @GetMapping("/ai/function/calc")
    public String calc(
            @RequestParam(value = "message", defaultValue = "计算 15 的 20% 是多少") String message) {
        return chatClient.prompt(message).tools("calculatePercentage").call().content();
    }

    /** 天气工具集。 */
    public static class WeatherTools {

        @Tool(name = "getWeather", description = "查询指定城市的当前天气")
        public String getWeather(@ToolParam(description = "城市名，如 北京") String city) {
            return city + "：晴，25°C，湿度 40%，东南风 3 级";
        }

        @Tool(name = "calculatePercentage", description = "计算一个数的百分比")
        public String calculatePercentage(
                @ToolParam(description = "数值") double value,
                @ToolParam(description = "百分比，如 20 表示 20%") double percent) {
            double result = value * percent / 100.0;
            return value + " 的 " + percent + "% = " + result;
        }
    }
}
