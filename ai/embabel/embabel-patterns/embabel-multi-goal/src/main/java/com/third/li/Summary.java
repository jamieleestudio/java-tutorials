package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 目标之一：摘要。 */
public record Summary(
        @JsonPropertyDescription("摘要正文") String content) {
}
