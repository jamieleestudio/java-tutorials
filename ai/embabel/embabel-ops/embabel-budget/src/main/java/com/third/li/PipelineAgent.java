package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 三步流水线：分析 -> 起草 -> 定稿。
 *
 * <p>刻意做成**多动作**（3 个动作），因为框架的**预算检查发生在动作之间**
 * （见 {@code AbstractAgentProcess} 的主循环）——所以"动作数上限"这类策略
 * 只有在多动作流程里才观察得到；单个动作内部的工具循环不会被它打断。
 *
 * <p>每个动作都声明了 {@code cost} / {@code value}（GOAP 规划器的成本与价值依据），
 * 便于在报告里对照"声明成本 vs 实际花费"。
 */
@Agent(description = "三步流水线：分析 -> 起草 -> 定稿（用于演示运行预算与熔断）")
public class PipelineAgent {

    @Action(description = "分析需求", cost = 0.01, value = 0.1)
    public Analysis analyze(UserInput userInput, Ai ai) {
        return new Analysis(ai.withDefaultLlm()
                .withId("budget-analyze")
                .generateText("请用 3 条要点分析下面的需求：\n" + userInput.getContent()));
    }

    @Action(description = "起草方案", cost = 0.02, value = 0.3)
    public Draft draft(Analysis analysis, Ai ai) {
        return new Draft(ai.withDefaultLlm()
                .withId("budget-draft")
                .generateText("请根据下面的分析要点，起草一个 150 字以内的方案：\n" + analysis.points()));
    }

    @Action(description = "定稿", cost = 0.03, value = 0.6)
    @AchievesGoal(description = "产出定稿")
    public Final finalize(Draft draft, Ai ai) {
        return new Final(ai.withDefaultLlm()
                .withId("budget-finalize")
                .generateText("请把下面的草稿定稿，输出最终方案：\n" + draft.content()));
    }
}
