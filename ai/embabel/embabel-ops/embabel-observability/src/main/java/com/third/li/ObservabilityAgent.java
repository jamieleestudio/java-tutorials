package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 两步执行的 Agent：便于观察"2 个动作 + 2 次 LLM 调用"的事件与耗时。
 */
@Agent(description = "可观测性示例：多步执行，事件监听器记录每一步")
public class ObservabilityAgent {

    @Action(description = "先列出回答要点")
    public TalkingPoints outline(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .creating(TalkingPoints.class)
                .fromPrompt("请为下面的问题列出 3 个回答要点：\n" + userInput.getContent());
    }

    @Action(description = "根据要点给出完整回答")
    @AchievesGoal(description = "产出回答")
    public Answer answer(TalkingPoints points, UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("observability-answer")
                .generateText("请根据以下要点回答问题：\n问题：%s\n要点：%s"
                        .formatted(userInput.getContent(), points));
        return new Answer(content);
    }
}
