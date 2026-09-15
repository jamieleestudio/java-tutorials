package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.core.Context;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.ContextRepository;

/**
 * 读取持久化上下文并据此回答的 Agent。
 *
 * <p>动作里通过 {@link ContextRepository} 按用户 ID 取回上下文（来自 Postgres），
 * 拿到 {@link UserProfile} 后写进提示词——这就是"跨会话记住用户"的最小实现。
 */
@Agent(description = "持久化上下文示例：读取用户画像并据此回答")
public class ContextAwareAgent {

    private final ContextRepository contextRepository;

    public ContextAwareAgent(ContextRepository contextRepository) {
        this.contextRepository = contextRepository;
    }

    @Action(description = "结合用户画像回答")
    @AchievesGoal(description = "产出回答")
    public ContextualAnswer answer(UserId userId, UserInput userInput, Ai ai) {
        Context context = contextRepository.findById(userId.value());
        UserProfile profile = context == null ? null : context.last(UserProfile.class);

        String content = ai.withDefaultLlm()
                .withId("persistence-answer")
                .generateText("""
                        已知用户画像：%s
                        请据此回答用户的问题；如果画像为空，请说明还不了解该用户。

                        用户问题：%s
                        """.formatted(profile == null ? "(无)" : profile, userInput.getContent()));

        return new ContextualAnswer(content, profile);
    }
}
