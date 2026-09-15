package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 使用 Embabel Agent API 实现的聊天 Agent。
 *
 * <p>{@code @Agent} 是 Spring 的构造型注解（等同 @Component），
 * 框架会自动扫描并注册该 Agent。{@code @Action} 标记一个可执行动作，
 * {@code @AchievesGoal} 声明该动作完成时即达成目标（返回 ChatReply）。
 *
 * <p>动作方法通过注入的 {@link Ai} 网关调用大模型，
 * 由 Embabel 的 GOAP 规划器决定动作的执行顺序。
 */
@Agent(description = "简单聊天 Agent，回答用户提出的问题")
public class ChatAgent {

    @Action(description = "根据用户问题生成回答")
    @AchievesGoal(description = "返回对用户问题的回答")
    public ChatReply chat(UserInput userInput, Ai ai) {
        String answer = ai.withDefaultLlm()
                .withId("chat-answer")
                .generateText("你是一个乐于助人的中文智能助手，请简洁、准确地回答用户的问题。\n\n用户问题：" + userInput.getContent());
        return new ChatReply(answer);
    }
}
