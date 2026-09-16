package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** RAG 回答（目标类型）。 */
public record VectorAnswer(
        @JsonPropertyDescription("问题") String question,
        @JsonPropertyDescription("检索到的片段（含相似度与来源）") List<VectorMatch> matches,
        @JsonPropertyDescription("基于片段的回答") String answer) {
}
