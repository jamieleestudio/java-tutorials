package com.third.li;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Agent 工具调用：@Tool 注解 + methodTools 注册。
 *
 * <p>工具定义与 Spring AI 完全一致（{@code @Tool} + {@code @ToolParam}），
 * 在 Agent 里用 {@code builder().methodTools(对象...)} 注册：
 * 框架自动解析方法签名生成 JSON Schema，模型决定何时调用、Agent 框架负责执行并回填结果。
 *
 * <p>对照 {@code ai/spring-ai/spring-ai-tools/spring-ai-function-calling}（ChatClient.tools）：
 * 语义相同，只是执行循环由 Agent 框架的 ReAct 节点接管。
 */
@Service
public class AgentToolsService {

    private final ReactAgent agent;

    public AgentToolsService(ChatModel chatModel) throws GraphStateException {
        this.agent = ReactAgent.builder()
                .name("daily-assistant")
                .description("时间与天气小助手")
                .systemPrompt("你是一个助手，回答时间与天气问题必须调用工具，不要凭空编造。回答简洁。")
                .model(chatModel)
                .methodTools(new TimeTools(), new WeatherTools())
                .build();
    }

    public String ask(String message) throws GraphRunnerException {
        return agent.call(message).getText();
    }

    /** 时间工具：当前本地时间。 */
    static class TimeTools {

        @Tool(description = "获取当前的日期时间")
        String now() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    /** 天气工具：模拟数据（演示用，避免真实 API 依赖）。 */
    static class WeatherTools {

        private static final Map<String, String> WEATHER = Map.of(
                "杭州", "晴，26°C，东南风 3 级",
                "北京", "多云，21°C，微风",
                "上海", "小雨，24°C，湿度 85%");

        @Tool(description = "查询指定城市的当前天气")
        String weather(
                @ToolParam(description = "城市名，如：杭州") String city) {
            return WEATHER.getOrDefault(city, city + "：暂无模拟数据（示例仅支持 杭州/北京/上海）");
        }
    }
}
