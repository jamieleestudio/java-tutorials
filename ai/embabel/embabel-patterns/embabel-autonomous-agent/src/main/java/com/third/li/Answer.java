package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 自主 Agent 的最终回答。 */
public record Answer(
        @JsonPropertyDescription("回答正文") String content) {
}
