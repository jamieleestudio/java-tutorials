package com.third.li;

import com.embabel.agent.api.annotation.LlmTool;

/**
 * 第一批工具（始终可用）。
 *
 * <p>注意 {@link #searchOrder} **返回的是 `Order` 对象而不是字符串**——
 * 框架的 {@code ToolCallSupport.convertResult} 会把非 String 的返回值包成
 * {@code Tool.Result.withArtifact(json, 对象)}，这样 artifact 收集器就能捕获它，
 * 进而让 `Order` 上的工具解锁（源码注释原话："so that ArtifactSinkingTool can capture it for tool chaining"）。
 */
public class OrderTools {

    @LlmTool(description = "按订单号查询订单。返回订单对象后，该订单的专属操作（打折、查状态）会变得可用")
    public Order searchOrder(
            @LlmTool.Param(description = "订单号，例如 A1001") String orderId) {
        return new Order(orderId, "已支付");
    }
}
