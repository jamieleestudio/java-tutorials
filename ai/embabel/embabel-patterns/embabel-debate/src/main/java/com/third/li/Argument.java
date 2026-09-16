package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 一位辩手的发言。 */
public record Argument(
        @JsonPropertyDescription("立场") String stance,
        @JsonPropertyDescription("论点要点") String points) {
}
