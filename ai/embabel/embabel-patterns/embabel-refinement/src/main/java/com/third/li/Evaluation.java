package com.third.li;

import com.embabel.agent.api.common.workflow.loop.Feedback;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 评审结果，实现 Embabel 的 {@link Feedback}（score 0~1，越大越好）。
 */
public record Evaluation(
        @JsonPropertyDescription("完成质量评分，0.0 最差，1.0 最好") double score,
        @JsonPropertyDescription("具体的改进建议") String suggestion) implements Feedback {

    @Override
    public double getScore() {
        return score;
    }
}
