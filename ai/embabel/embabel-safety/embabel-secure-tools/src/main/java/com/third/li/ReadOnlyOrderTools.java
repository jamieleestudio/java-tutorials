package com.third.li;

import com.embabel.agent.api.annotation.LlmTool;

/**
 * **最小权限**工具集：只暴露**只读**查询，且返回值已脱敏。
 *
 * <p>注意这里**故意没有**导出/修改类工具——Agent 能做什么由"暴露了哪些工具"决定，
 * 这是最有效的安全边界（比事后过滤更可靠）。需要写操作时应走人工审批流程
 * （见 {@code embabel-hitl} 的 `WaitFor.confirmation`）。
 */
public class ReadOnlyOrderTools {

    @LlmTool(description = "按订单号查询订单状态（只读，返回结果已脱敏）")
    public String queryOrder(
            @LlmTool.Param(description = "订单号，例如 A1001") String orderId) {
        return "订单 %s：已发货；收件人：张*；电话：138****0000".formatted(orderId);
    }

    @LlmTool(description = "列出当前用户最近的订单（只读）")
    public String listOrders() {
        return "A1001 已发货；A1002 待付款（已脱敏）";
    }
}
