package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 带护栏的聊天 Agent。
 *
 * <p>通过 {@code withGuardRails(...)} 给单次 PromptRunner 挂上护栏：
 * 输入在发送给模型之前校验，输出在模型返回之后校验。
 */
@Agent(description = "带护栏的聊天 Agent：输入注入检测 + 输出敏感信息检查")
public class GuardedChatAgent {

    @Action(description = "在护栏保护下回答问题")
    @AchievesGoal(description = "返回通过护栏检查的回复")
    public ChatReply chat(UserInput userInput, Ai ai) {
        String answer = ai.withDefaultLlm()
                .withGuardRails(new InjectionGuardRail(), new SensitiveOutputGuardRail())
                .withId("guarded-chat")
                .generateText("你是企业智能助手，请用简洁的中文回答用户问题。\n用户："
                        + userInput.getContent());
        return new ChatReply(answer);
    }
}
