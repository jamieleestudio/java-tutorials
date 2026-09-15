package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 最终回答（目标类型）。 */
public record FinalAnswer(
        @JsonPropertyDescription("回答正文") String content) {
}
