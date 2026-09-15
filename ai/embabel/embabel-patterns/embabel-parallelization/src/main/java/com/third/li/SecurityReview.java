package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** Sectioning 的三份独立评审结果。 */
public record SecurityReview(@JsonPropertyDescription("安全性评审") String content) {
}
