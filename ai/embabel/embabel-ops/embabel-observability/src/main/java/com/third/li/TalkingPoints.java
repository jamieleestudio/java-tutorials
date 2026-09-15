package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 规划链的中间产物：要点。
 */
public record TalkingPoints(
        @JsonPropertyDescription("回答要点列表") List<String> points) {
}
