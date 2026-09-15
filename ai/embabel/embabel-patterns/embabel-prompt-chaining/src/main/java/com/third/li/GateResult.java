package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 关卡（gate）的判定结果：是否通过、原因，以及原始提纲。
 *
 * <p>注意：真正的"拦截"不是靠这个布尔值，而是靠 {@link WritingAgent} 上
 * {@code @Condition} + {@code @Action(pre = ...)} 的组合——gate 不通过时，
 * 后续步骤的前置条件不满足，链自然停止。
 */
public record GateResult(
        @JsonPropertyDescription("提纲") Outline outline,
        @JsonPropertyDescription("是否通过关卡") boolean passed,
        @JsonPropertyDescription("判定原因") String reason) {
}
