package com.third.li;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务工具集：用 {@code @Tool} 注解声明，AgentScope 会自动提取 schema 并注册到 Toolkit。
 *
 * <p>与 Embabel 的 {@code @LlmTool} 对比：
 * <ul>
 *   <li>Embabel: {@code @LlmTool(category = "read", description = "...")} + {@code @LlmTool.Param}</li>
 *   <li>AgentScope: {@code @Tool(name = "...", description = "...")} + {@code @ToolParam(description = "...")}</li>
 * </ul>
 * 两者都是"注解在方法上、框架反射提取 schema"的思路，但 AgentScope 的 {@code @Tool}
 * 还带了 {@code readOnly}、{@code concurrencySafe}、{@code dangerousFiles} 等安全属性——
 * 这些在后面的 Permission 模块里会用到。
 */
@Component
public class OrderQueryTools {

    private final Map<String, String> orders = new HashMap<>();

    public OrderQueryTools() {
        orders.put("A1001", "已支付，金额 ¥299，2026-09-01 下单");
        orders.put("A1002", "已发货，金额 ¥899，2026-08-15 下单");
        orders.put("A1003", "待付款，金额 ¥1299，2026-09-10 下单");
    }

    @Tool(name = "queryOrder", description = "按订单号查询订单状态与金额")
    public String queryOrder(
            @ToolParam(name = "orderId", description = "订单号，例如 A1001") String orderId) {
        String info = orders.get(orderId);
        return info != null ? "订单 " + orderId + "：" + info : "未找到订单 " + orderId;
    }

    @Tool(name = "getCurrentTime", description = "获取当前时间")
    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Tool(name = "calculateDiscount", description = "根据原价和折扣率计算折后价")
    public String calculateDiscount(
            @ToolParam(name = "originalPrice", description = "原价（元）") double originalPrice,
            @ToolParam(name = "discountRate", description = "折扣率，0~1 之间，例如 0.8 表示 8 折") double discountRate) {
        double finalPrice = originalPrice * discountRate;
        return "原价 %.2f 元，折扣率 %.1f%%，折后价 %.2f 元".formatted(
                originalPrice, discountRate * 100, finalPrice);
    }
}