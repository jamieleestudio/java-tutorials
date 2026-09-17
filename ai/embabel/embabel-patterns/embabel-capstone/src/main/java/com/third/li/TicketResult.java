package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 工单处理结果（目标类型）。 */
public record TicketResult(
        @JsonPropertyDescription("处理状态：REJECTED / AUTO_APPROVED / PENDING_CONFIRMATION / CONFIRMED / DECLINED")
        String status,
        @JsonPropertyDescription("处理说明（含依据条款）") String summary,
        @JsonPropertyDescription("订单号") String orderId,
        @JsonPropertyDescription("涉及金额") double amount,
        @JsonPropertyDescription("实际走过的阶段") List<String> stages) {
}
