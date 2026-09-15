package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 被测的最小 Agent：一个动作、一个目标。
 *
 * <p>它的"可规划性"可以被离线校验（见 {@code AgentPlanValidationTest}），
 * 动作逻辑可以被 Mockito 单测覆盖（见 {@code OutlineAgentTest}）。
 */
@Agent(description = "测试示例用的最小 Agent：为问题列出回答要点")
public class OutlineAgent {

    @Action(description = "列出回答要点")
    @AchievesGoal(description = "产出要点")
    public TalkingPoints outline(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .creating(TalkingPoints.class)
                .fromPrompt("请为下面的问题列出 3 个回答要点：\n" + userInput.getContent());
    }
}
