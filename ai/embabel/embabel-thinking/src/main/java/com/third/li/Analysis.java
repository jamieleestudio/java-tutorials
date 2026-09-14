package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 答案与推理过程的分离结果。
 */
public record Analysis(
        @JsonPropertyDescription("最终答案") String answer,
        @JsonPropertyDescription("是否成功提取到推理过程") boolean thinkingExtracted,
        @JsonPropertyDescription("模型的推理过程") String thinking,
        @JsonPropertyDescription("降级说明，无降级时为 null") String note) {
}
