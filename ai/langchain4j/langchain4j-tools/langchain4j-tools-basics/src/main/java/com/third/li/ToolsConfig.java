package com.third.li;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具定义与注册：{@code @Tool} 注解方法 + {@code AiServices.tools(...)} 注入。
 *
 * <p>LangChain4j 会反射读取 {@code @Tool(description)} 与 {@code @P(description)}
 * 生成 JSON Schema 供模型决策调用 —— 与 Spring AI 的 @Tool、SAA 的 @Tool 同构。
 */
@Configuration
public class ToolsConfig {

    public static class CalculatorTools {

        @Tool("计算两个数的四则运算")
        double calculate(@P("第一个数") double a, @P("第二个数") double b, @P("运算符") String op) {
            return switch (op) {
                case "+" -> a + b;
                case "-" -> a - b;
                case "*" -> a * b;
                case "/" -> a / b;
                default -> throw new IllegalArgumentException("未知运算符: " + op);
            };
        }
    }

    public static class WeatherTools {

        @Tool("查询指定城市的当前天气（模拟数据）")
        String weather(@P("城市名，如：杭州") String city) {
            return switch (city) {
                case "杭州" -> "晴，26°C，东南风 3 级";
                case "北京" -> "多云，21°C，微风";
                default -> city + "：暂无模拟数据（示例仅支持 杭州/北京）";
            };
        }
    }

    public interface ToolAssistant {

        @SystemMessage("你是助手。数学计算与天气查询必须调用工具，不要心算或编造。回答简洁。")
        String chat(String message);
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
    public ToolAssistant toolAssistant(ChatModel chatModel) {
        return AiServices.builder(ToolAssistant.class)
                .chatModel(chatModel)
                .tools(new CalculatorTools(), new WeatherTools())
                .build();
    }
}
