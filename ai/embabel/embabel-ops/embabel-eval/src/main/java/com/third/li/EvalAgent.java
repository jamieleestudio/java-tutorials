package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

import java.util.ArrayList;
import java.util.List;

/**
 * 评估（eval harness）模式：**数据集 → 跑被测系统 → LLM 评审打分 → 报告**。
 *
 * <p>为什么需要它：单元测试只能验证"结构/调用"，而 Agent 的输出是自然语言，
 * 需要**按语义评分**。评估集是 Agent 从"能跑"到"可靠"的关键工程实践：
 * <ul>
 *   <li>每条用例 = 问题 + **判定标准**（criterion）</li>
 *   <li>被测系统（这里是一段简单问答）逐条作答</li>
 *   <li>另一个 LLM 调用充当**评审**，按标准给 0~1 分</li>
 *   <li>汇总成通过率/平均分报告，用于回归对比（改提示词/换模型前后各跑一次）</li>
 * </ul>
 *
 * <p>生产上还会：固定随机性、多评审员投票（见 {@code embabel-parallelization} 的 voting）、
 * 把报告写进 CI（见 {@code embabel-testing}）。
 */
@Agent(description = "评估 harness：逐条跑用例并用 LLM 评审打分")
public class EvalAgent {

    private static final double PASS_THRESHOLD = 0.7;

    @Action(description = "运行评估集并产出报告")
    @AchievesGoal(description = "产出评估报告")
    public EvalReport run(UserInput userInput, Ai ai) {
        List<CaseResult> results = new ArrayList<>();

        for (Dataset.EvalCase evalCase : Dataset.CASES) {
            // 1) 被测系统作答
            String answer = ai.withDefaultLlm()
                    .withId("eval-answer-" + evalCase.id())
                    .generateText("请简洁准确地回答下面的问题：\n" + evalCase.question());

            // 2) LLM 评审打分
            Judge judge = ai.withDefaultLlm()
                    .withId("eval-judge-" + evalCase.id())
                    .creating(Judge.class)
                    .fromPrompt("""
                            你是严格的评审员。请判断下面的回答是否满足判定标准，给出 0~1 的分数与理由。

                            判定标准：%s
                            回答：%s
                            """.formatted(evalCase.criterion(), answer));

            results.add(new CaseResult(
                    evalCase.id(), answer, judge.score(),
                    judge.score() >= PASS_THRESHOLD, judge.reason()));
        }

        int passed = (int) results.stream().filter(CaseResult::passed).count();
        double average = results.stream().mapToDouble(CaseResult::score).average().orElse(0);

        return new EvalReport(results.size(), passed, average, results);
    }
}
