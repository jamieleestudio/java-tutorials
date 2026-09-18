package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolGroup;
import io.agentscope.core.tool.ToolGroupScope;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具分组 Agent：把工具按职责分成多个 {@link ToolGroup}，用
 * {@link Toolkit#createToolGroup(String, String, boolean, ToolGroupScope)} 创建组、
 * {@link Toolkit#addToolToGroup(String, String)} 把工具归入组，
 * {@link Toolkit#setActiveGroups(java.util.List)} 切换当前激活的组。
 *
 * <p>只有属于"激活组"的工具才会暴露给模型——这样可以在一个 Toolkit 里放很多工具，
 * 但每次只让模型看到与当前任务相关的那一子集，减少干扰、提升选工具准确率。
 *
 * <p>本例定义两个组：
 * <ul>
 *   <li>{@code time-group} —— 时间相关工具（默认激活）</li>
 *   <li>{@code order-group} —— 订单相关工具（按需激活）</li>
 * </ul>
 */
@Component
public class ToolGroupAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-toolkit-groups";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public ToolGroupAgent(
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

    public String chatWithOrderGroup(String message) {
        agent().getDelegate().getToolkit().setActiveGroups(java.util.List.of("order-group"));
        try {
            return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
        } finally {
            agent().getDelegate().getToolkit().setActiveGroups(java.util.List.of("time-group"));
        }
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(new TimeTools());
                    toolkit.registerTool(new OrderTools());

                    toolkit.registerToolGroup(ToolGroup.builder()
                            .name("time-group")
                            .description("时间相关工具")
                            .active(true)
                            .scope(ToolGroupScope.META)
                            .tools(java.util.Set.of("getCurrentTime"))
                            .build());
                    toolkit.registerToolGroup(ToolGroup.builder()
                            .name("order-group")
                            .description("订单相关工具")
                            .active(false)
                            .scope(ToolGroupScope.META)
                            .tools(java.util.Set.of("queryOrder", "calculateDiscount"))
                            .build());

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("toolkit-groups")
                            .sysPrompt("你是一个智能助手，能使用工具分组。默认可查询时间；按需可切换到订单查询。")
                            .model(model)
                            .toolkit(toolkit)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("toolgroup-demo").userId("alice").build();
    }

    public static class TimeTools {

        @Tool(name = "getCurrentTime", description = "获取当前时间")
        public String getCurrentTime() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public static class OrderTools {

        private final Map<String, String> orders = new HashMap<>();

        public OrderTools() {
            orders.put("A1001", "已支付，金额 ¥299");
            orders.put("A1002", "已发货，金额 ¥899");
        }

        @Tool(name = "queryOrder", description = "按订单号查询订单状态")
        public String queryOrder(@ToolParam(name = "orderId", description = "订单号") String orderId) {
            return orders.getOrDefault(orderId, "未找到订单 " + orderId);
        }

        @Tool(name = "calculateDiscount", description = "计算折后价")
        public String calculateDiscount(
                @ToolParam(name = "originalPrice", description = "原价") double originalPrice,
                @ToolParam(name = "discountRate", description = "折扣率 0~1") double discountRate) {
            return "折后价：" + (originalPrice * discountRate);
        }
    }
}