package com.third.li;

import com.embabel.agent.api.annotation.LlmTool;
import com.embabel.agent.api.annotation.UnfoldingTools;

import java.util.List;

/**
 * **渐进式工具（progressive tool disclosure）**：用 {@link UnfoldingTools} 把一组工具
 * 折叠成一个"门面工具"——模型先看到门面（名称 + 描述），调用后才展开出具体工具。
 *
 * <p>好处：工具很多时，初始提示词里只放一个门面，降低模型的认知负担与 token 开销；
 * 还可用 {@code category} 把工具分组，让模型按需选择类别。
 *
 * <p>本示例把工具分成 read / write 两类（写操作更危险，模型按需展开）。
 */
@UnfoldingTools(
        name = "order_ops",
        description = "订单相关操作（查询/取消）。先调用本工具查看可用的具体操作")
public class OrderTools {

    @LlmTool(category = "read", description = "按订单号查询订单状态")
    public String queryOrder(
            @LlmTool.Param(description = "订单号，例如 A1001") String orderId) {
        return "订单 %s：已发货，预计明天送达（模拟数据）".formatted(orderId);
    }

    @LlmTool(category = "read", description = "列出最近的订单")
    public List<String> listOrders() {
        return List.of("A1001 已发货", "A1002 待付款", "A1003 已完成");
    }

    @LlmTool(category = "write", description = "取消订单（危险操作，请先与用户确认）")
    public String cancelOrder(
            @LlmTool.Param(description = "订单号") String orderId) {
        return "订单 %s 已取消（模拟操作）".formatted(orderId);
    }
}
