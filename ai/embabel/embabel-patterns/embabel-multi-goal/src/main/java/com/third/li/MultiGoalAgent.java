package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 多目标 + 自动选择模式。
 *
 * <p>同一个 Agent 声明**多个目标**（三个 {@code @AchievesGoal}，产出不同类型）。
 * 调用方只给输入，**不指定**要哪个目标：平台用**排序器**在这些目标之间选择
 * （配置 {@code embabel.agent.platform.ranking.llm} 后由 LLM 排序，否则用默认实现）。
 *
 * <p><b>语义提醒</b>：{@code AgentInvocation.create(platform, X.class)} 的 {@code X} 只用于选 **Agent**
 * （找第一个"有目标产出类型可赋给 X"的 Agent），<b>不会</b>在同一 Agent 的多个目标间路由。
 * 要让调用方精确指定产出类型，可靠做法是**一个 Agent 一个目标**。
 */
@Agent(description = "多目标 Agent：同一输入可产出摘要 / 评审 / 步骤，由调用方声明所需类型")
public class MultiGoalAgent {

    @Action(description = "产出摘要")
    @AchievesGoal(description = "产出摘要")
    public Summary summarize(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("multi-goal-summary")
                .generateText("请用 100 字以内总结下面内容的要点：\n" + userInput.getContent());
        return new Summary(content);
    }

    @Action(description = "产出评审意见")
    @AchievesGoal(description = "产出评审意见")
    public Critique critique(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("multi-goal-critique")
                .generateText("请从可行性、风险、改进建议三方面评审下面的内容：\n" + userInput.getContent());
        return new Critique(content);
    }

    @Action(description = "产出行动步骤")
    @AchievesGoal(description = "产出行动步骤")
    public Steps toSteps(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Steps.class)
                .fromPrompt("请把下面内容拆成 3~5 个可执行的行动步骤：\n" + userInput.getContent());
    }
}
