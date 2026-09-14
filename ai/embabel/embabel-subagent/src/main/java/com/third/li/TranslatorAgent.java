package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;

/**
 * 专家 Agent（子 Agent）：只负责翻译。
 *
 * <p>它会被 {@link CoordinatorAgent} 当作工具调用，运行在独立的子流程中，
 * 共享父流程的黑板上下文。
 */
@Agent(description = "翻译专家：把给定文本翻译成目标语言")
public class TranslatorAgent {

    @Action(description = "翻译文本")
    @AchievesGoal(description = "产出译文")
    public Translation translate(TranslationRequest request, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Translation.class)
                .fromPrompt("把下面的文本翻译成 %s，只返回译文：\n%s"
                        .formatted(request.targetLanguage(), request.text()));
    }
}
