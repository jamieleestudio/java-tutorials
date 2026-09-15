package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 性能评审。 */
public record PerformanceReview(@JsonPropertyDescription("性能评审") String content) {
}
