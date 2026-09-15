package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 回答提纲。 */
public record Outline(
        @JsonPropertyDescription("提纲小节") List<String> sections) {
}
