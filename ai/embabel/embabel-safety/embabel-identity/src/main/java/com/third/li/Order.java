package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 一条订单（带租户标记，用来演示数据隔离）。 */
public record Order(
        @JsonPropertyDescription("订单号") String id,
        @JsonPropertyDescription("所属租户") String tenantId,
        @JsonPropertyDescription("金额") double amount) {
}
