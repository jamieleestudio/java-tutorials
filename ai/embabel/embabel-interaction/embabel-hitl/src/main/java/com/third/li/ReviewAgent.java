package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.core.hitl.WaitFor;
import com.embabel.agent.domain.io.UserInput;

/**
 * 确认型人机协同 Agent。
 *
 * <p>{@link WaitFor#confirmation(Object, String)} 会暂停流程，等待人工确认；
 * 确认通过后 payload 被提升到黑板，方法继续执行并拿到该值；
 * 拒绝时流程保持暂停（不会产出结果）。
 *
 * <p>注意：确认的 payload 类型要与动作的返回类型一致，
 * 这样人工确认后目标才能被满足。
 */
@Agent(description = "确认型 HITL：生成草稿，人工确认后才发布")
public class ReviewAgent {

    @Action(description = "生成草稿并请求人工确认")
    @AchievesGoal(description = "产出已确认的回复")
    public ChatReply review(UserInput userInput, Ai ai) {
        String draft = ai.withDefaultLlm()
                .withId("review-draft")
                .generateText("请撰写一段简洁的回复草稿，只输出正文：\n" + userInput.getContent());
        return WaitFor.confirmation(new ChatReply(draft), "是否发布这份草稿？");
    }
}
