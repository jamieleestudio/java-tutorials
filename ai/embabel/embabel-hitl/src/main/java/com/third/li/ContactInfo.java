package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 表单绑定的目标类型。
 */
public record ContactInfo(
        @JsonPropertyDescription("姓名") String name,
        @JsonPropertyDescription("邮箱") String email) {
}
