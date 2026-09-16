package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 辩论结论（目标类型）。 */
public record DebateResult(
        @JsonPropertyDescription("辩题") String topic,
        @JsonPropertyDescription("各方论点") List<Argument> arguments,
        @JsonPropertyDescription("裁判的综合结论") String conclusion) {
}
