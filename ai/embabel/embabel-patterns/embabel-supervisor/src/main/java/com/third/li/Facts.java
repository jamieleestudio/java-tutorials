package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 收集到的事实要点。 */
public record Facts(
        @JsonPropertyDescription("事实要点列表") List<String> facts) {
}
