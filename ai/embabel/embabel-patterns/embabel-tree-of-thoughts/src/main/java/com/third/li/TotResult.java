package com.third.li;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** 搜索结果（目标类型）：最佳思路 + 完整搜索轨迹。 */
public record TotResult(
        @JsonPropertyDescription("问题") String problem,
        @JsonPropertyDescription("最佳思路") String best,
        @JsonPropertyDescription("搜索过程中探索过的所有候选（含评分）") List<Candidate> explored,
        @JsonPropertyDescription("展开深度") int depth) {
}
