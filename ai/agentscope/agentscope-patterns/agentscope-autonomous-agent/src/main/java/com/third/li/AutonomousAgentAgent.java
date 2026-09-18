package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Autonomous Agent 模式（自主 Agent）。
 *
 * <p>AgentScope 的 ReAct 循环<b>天然就是自主 Agent</b>：模型在循环中自主决定
 * 调用哪个工具、观察结果、继续推理，直到任务完成或达到 {@code maxIters}。
 *
 * <p>与 Embabel 的 {@code embabel-autonomous-agent} 对照——Embabel 需要规划器推导
 * 动作序列 + 环境反馈；AgentScope 的模型直接在循环里"推理-行动"。
 *
 * <p>本模块演示：
 * <ul>
 *   <li>内置工具集（计算器 + 天气 + 时间）</li>
 *   <li>{@code maxIters} 停止条件（防无限循环）</li>
 *   <li>{@code ExecutionConfig.maxAttempts} 错误重试（工具调用失败自动重试）</li>
 * </ul>
 */
@Component
public class AutonomousAgentAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public AutonomousAgentAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();

                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(new CalculatorTool());
                    toolkit.registerTool(new WeatherTool());
                    toolkit.registerTool(new TimeTool());

                    ExecutionConfig toolConfig = ExecutionConfig.builder()
                            .maxAttempts(3)       // 工具调用失败自动重试 3 次
                            .timeout(java.time.Duration.ofSeconds(15))
                            .build();

                    local = HarnessAgent.builder()
                            .name("autonomous-agent")
                            .sysPrompt("""
                                    你是一个自主 Agent。你有以下工具：
                                    - calculator：数学计算
                                    - getWeather：查询天气
                                    - getTime：查询当前时间

                                    根据用户请求自主决定调用哪些工具，观察结果后给出最终回答。
                                    不要假设，使用工具获取真实信息。
                                    """)
                            .model(model)
                            .toolkit(toolkit)
                            .toolExecutionConfig(toolConfig)
                            .maxIters(8)          // 停止条件：最多 8 轮
                            .workspace(Paths.get(".agentscope/agentscope-autonomous-agent"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("autonomous-agent").userId("alice").build();
    }

    /** 计算器工具。 */
    public static class CalculatorTool {
        @Tool(name = "calculator", description = "执行数学计算，支持 + - * / 和括号")
        public String calculate(
                @ToolParam(name = "expression", description = "数学表达式，如 (3+5)*2") String expression) {
            try {
                double result = new javax.script.ScriptEngineManager()
                        .getEngineByName("js")
                        .eval(expression)
                        instanceof Number n ? n.doubleValue()
                        : Double.parseDouble(String.valueOf(
                                new javax.script.ScriptEngineManager()
                                        .getEngineByName("js").eval(expression)));
                return expression + " = " + result;
            } catch (Exception e) {
                return "计算失败：" + e.getMessage();
            }
        }
    }

    /** 天气工具。 */
    public static class WeatherTool {
        @Tool(name = "getWeather", description = "查询指定城市的当前天气")
        public String weather(
                @ToolParam(name = "city", description = "城市名，如 北京") String city) {
            return city + " 当前天气：晴，25°C，湿度 40%";
        }
    }

    /** 时间工具。 */
    public static class TimeTool {
        private final AtomicInteger calls = new AtomicInteger(0);

        @Tool(name = "getTime", description = "查询当前日期和时间")
        public String time() {
            return "当前时间：" + java.time.LocalDateTime.now() + "（调用次数：" + calls.incrementAndGet() + "）";
        }
    }
}