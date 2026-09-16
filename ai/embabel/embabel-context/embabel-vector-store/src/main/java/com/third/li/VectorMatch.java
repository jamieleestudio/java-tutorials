package com.third.li;

import com.embabel.common.core.types.Described;
import com.embabel.common.core.types.Named;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 一次向量检索的命中结果。
 *
 * <p>实现 {@link Named} + {@link Described} 是为了能被框架的 {@code Ranker} 排序——
 * {@code LlmRanker.rank(description, userInput, rankables)} 要求候选类型同时实现这两个接口
 * （它会把 {@code name} 与 {@code description} 放进提示词让模型打分）。
 */
public record VectorMatch(
        @JsonPropertyDescription("文档 ID") String id,
        @JsonPropertyDescription("标题") String title,
        @JsonPropertyDescription("内容") String content,
        @JsonPropertyDescription("来源（元数据）") String source,
        @JsonPropertyDescription("余弦相似度（1 - 余弦距离），越大越相关") double score)
        implements Named, Described {

    /** 排序时给模型看的名字。 */
    @Override
    @JsonIgnore
    public String getName() {
        return title;
    }

    /** 排序时给模型看的内容。 */
    @Override
    @JsonIgnore
    public String getDescription() {
        return content;
    }

    /** 重排后附上 LLM 相关性评分，便于对照两阶段的排序差异。 */
    public VectorMatch withScore(double newScore) {
        return new VectorMatch(id, title, content, source, newScore);
    }
}
