package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PlannerType;
import com.embabel.agent.domain.io.UserInput;

/**
 * GOAP（Goal Oriented Action Planning）规划器示例。
 *
 * <p>GOAP 从目标出发反向规划：{@code UserInput -> TalkingPoints -> GoapAnswer}，
 * 中间步骤不需要手写。它<b>必须有目标</b>（{@code @AchievesGoal}）。
 */
@Agent(planner = PlannerType.GOAP,
        description = "GOAP 规划器示例：从目标反向推导动作链，必须有目标")
public class GoapPlannerAgent {

    @Action(description = "先为问题列出回答要点")
    public TalkingPoints outline(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .creating(TalkingPoints.class)
                .fromPrompt("请为下面的问题列出 3 个回答要点：\n" + userInput.getContent());
    }

    @Action(description = "根据要点给出完整回答")
    @AchievesGoal(description = "产出回答")
    public GoapAnswer answer(TalkingPoints points, UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("goap-answer")
                .generateText("请根据以下要点回答问题，输出完整段落：\n问题：%s\n要点：%s"
                        .formatted(userInput.getContent(), points));
        return new GoapAnswer(content, PlannerType.GOAP.name());
    }
}
