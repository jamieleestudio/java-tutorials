package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 被 A2A 暴露的 Agent（服务端技能）。
 *
 * <p>它的目标会被 A2A 服务端转换成 Agent Card 里的 skill；
 * 远端通过 {@code message/send} 发来的文本会作为 {@link UserInput} 进入本 Agent。
 */
@Agent(description = "A2A 示例 Agent：回答用户提出的问题")
public class SupportAgent {

    @Action(description = "回答用户问题")
    @AchievesGoal(description = "产出回答")
    public ChatReply answer(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("a2a-answer")
                .generateText("你是一个通过 A2A 协议被调用的智能体，请简洁准确地回答：\n"
                        + userInput.getContent());
        return new ChatReply(content);
    }
}
