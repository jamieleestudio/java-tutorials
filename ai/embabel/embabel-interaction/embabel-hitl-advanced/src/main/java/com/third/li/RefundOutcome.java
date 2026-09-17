package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 退款处理结果（目标类型）。 */
public record RefundOutcome(
        @JsonPropertyDescription("订单号") String orderId,
        @JsonPropertyDescription("金额") double amount,
        @JsonPropertyDescription("是否向用户索取了账号信息") boolean askedUser,
        @JsonPropertyDescription("处理结果") String result) {
}
