package com.third.li;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * MCP 服务端：用 @Tool 定义工具，MethodToolCallbackProvider 注册给 MCP。
 *
 * <p>{@code spring-ai-starter-mcp-server-webmvc} 在 SSE 端点（默认 /sse）上提供
 * MCP 协议服务；任意 MCP 客户端（包括本仓库 8517 的 saa-mcp-client、Claude Desktop、
 * npx 工具等）都能发现并调用这里注册的工具。
 *
 * <p>生产上 SAA 还支持把 MCP server 注册进 Nacos（nacos-mcp-router），
 * 实现"服务发现 + 工具市场"，这是阿里生态的特色玩法。
 */
@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider saaTools() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(new WeatherTool(), new OrderTool())
                .build();
    }

    /** 天气查询工具（模拟数据）。 */
    static class WeatherTool {

        @Tool(description = "查询指定城市的当前天气")
        String weather(@ToolParam(description = "城市名，如：杭州") String city) {
            int temp = ThreadLocalRandom.current().nextInt(18, 30);
            return city + "：晴转多云，" + temp + "°C，湿度 60%（模拟数据）";
        }
    }

    /** 订单查询工具（模拟数据）。 */
    static class OrderTool {

        private static final Map<String, String> ORDERS = Map.of(
                "1001", "已发货：Spring AI Alibaba 实战手册 × 1，预计明日达",
                "1002", "已签收：机械键盘 × 1",
                "1003", "待付款：显示器 × 1");

        @Tool(description = "按订单号查询订单状态")
        String order(@ToolParam(description = "订单号，如：1001") String orderId) {
            return ORDERS.getOrDefault(orderId, "订单 " + orderId + " 不存在（示例仅支持 1001/1002/1003）");
        }
    }
}
