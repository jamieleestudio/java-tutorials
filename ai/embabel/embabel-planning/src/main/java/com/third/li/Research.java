package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 调研结果：规划流水线的第一个中间产物。
 */
public record Research(
        @JsonPropertyDescription("调研主题") String topic,
        @JsonPropertyDescription("关键要点列表") List<String> keyPoints) {
}
