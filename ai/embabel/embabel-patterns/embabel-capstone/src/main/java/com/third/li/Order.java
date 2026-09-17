package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 订单（工具返回的领域对象）。 */
public record Order(
        @JsonPropertyDescription("订单号") String id,
        @JsonPropertyDescription("状态") String status,
        @JsonPropertyDescription("金额") double amount,
        @JsonPropertyDescription("下单日期") String createdAt) {
}
