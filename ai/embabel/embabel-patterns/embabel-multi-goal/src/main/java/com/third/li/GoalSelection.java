package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 自动选择的结果：平台选了哪个目标、产出是什么。
 */
public record GoalSelection(
        @JsonPropertyDescription("平台自动选中的目标") String goal,
        @JsonPropertyDescription("进程终态") String status,
        @JsonPropertyDescription("目标产出") Object result) {
}
