package com.third.li;

import com.embabel.agent.api.annotation.LlmTool;
import org.springframework.stereotype.Component;

/**
 * 业务工具：查订单。
 *
 * <p>返回**领域对象**（不是字符串）——框架会自动把它包成 artifact
 * （见 {@code embabel-tool-chaining}），所以工具产物是"一等公民"，
 * 后续可以用 `withToolChainingFrom` 解锁订单专属操作。
 */
@Component
public class OrderTools {

    @LlmTool(description = "按订单号查询订单的状态、金额与下单日期")
    public Order queryOrder(
            @LlmTool.Param(description = "订单号，例如 A1001") String orderId) {
        return new Order(orderId, "已支付", 1200.0, "2026-09-01");
    }
}
