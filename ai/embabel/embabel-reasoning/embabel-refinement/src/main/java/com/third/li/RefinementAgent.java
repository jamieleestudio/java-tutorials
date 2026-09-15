package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 自评迭代 Agent（Evaluator-Optimizer 模式）。
 *
 * <p>每一轮：生成/改进文本 → 让模型按 {@link Evaluation} 打分并给出建议；
 * 分数达到阈值就提前结束，否则带着评审意见再改一轮，最多 {@value #MAX_ITERATIONS} 轮。
 *
 * <p>Embabel 在 Kotlin DSL 里提供了同款原语 {@code RepeatUntilAcceptable}
 * （负责生成任务动作、评估动作、接受条件与"取历史最优"的收敛动作）。
 * 这里用 Java 手写同一模式，便于看清每一轮的控制流。
 */
@Agent(description = "Evaluator-Optimizer 自评迭代：生成 -> 评分 -> 不满意则带着意见重做")
public class RefinementAgent {

    private static final int MAX_ITERATIONS = 3;
    private static final double THRESHOLD = 0.85;

    @Action(description = "迭代生成并自评，直到达标或达到最大轮数")
    @AchievesGoal(description = "产出经过自评迭代的文本")
    public RefinedText refine(UserInput userInput, Ai ai) {
        String task = userInput.getContent();
        String draft = null;
        Evaluation evaluation = null;
        int attempts = 0;

        for (int round = 1; round <= MAX_ITERATIONS; round++) {
            attempts = round;
            String draftPrompt = (draft == null)
                    ? "请完成下面的写作任务，只输出正文：\n" + task
                    : """
                    请根据评审意见改进下面的文本，直接输出改进后的完整正文。
                    任务：%s
                    当前版本：
                    %s
                    评审意见：%s
                    """.formatted(task, draft, evaluation.suggestion());

            draft = ai.withDefaultLlm()
                    .withId("draft-" + round)
                    .generateText(draftPrompt);

            evaluation = ai.withDefaultLlm()
                    .withId("evaluate-" + round)
                    .creating(Evaluation.class)
                    .fromPrompt("""
                            请评估下面文本对任务的完成质量，给出 0~1 的 score 和具体的改进建议。
                            任务：%s
                            文本：
                            %s
                            """.formatted(task, draft));

            if (evaluation.score() >= THRESHOLD) {
                break;
            }
        }

        return new RefinedText(draft, evaluation.score(), attempts, evaluation.suggestion());
    }
}
