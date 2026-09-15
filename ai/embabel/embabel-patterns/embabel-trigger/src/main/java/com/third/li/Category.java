package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 输入分类（演示用，不参与目标达成）。 */
public record Category(
        @JsonPropertyDescription("问题类别") String value) {
}
