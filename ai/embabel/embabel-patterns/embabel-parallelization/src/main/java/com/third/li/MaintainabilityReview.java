package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 可维护性评审。 */
public record MaintainabilityReview(@JsonPropertyDescription("可维护性评审") String content) {
}
