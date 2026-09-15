package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 多模型协作的回答。
 */
public record Answer(
        @JsonPropertyDescription("回答正文") String content,
        @JsonPropertyDescription("本次使用的模型路由") String routing) {
}
