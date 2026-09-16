package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 回答（目标类型）。 */
public record Reply(
        @JsonPropertyDescription("回答正文") String content) {
}
