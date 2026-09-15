package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 目标之一：行动步骤。 */
public record Steps(
        @JsonPropertyDescription("步骤列表") List<String> steps) {
}
