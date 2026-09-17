package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 退款所需的账号信息——**由用户在流程中途提供**。
 *
 * <p>{@code TypeRequest} 会把这个类型作为"表单 schema"交给 UX 层，
 * 所以字段描述会直接变成表单提示。
 */
public record RefundAccount(
        @JsonPropertyDescription("收款账号，例如 6222 0000 1234 5678") String account,
        @JsonPropertyDescription("开户行，例如 招商银行") String bank,
        @JsonPropertyDescription("收款人姓名") String holder) {
}
