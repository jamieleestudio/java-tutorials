package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 多步研究写作 Agent。
 *
 * <p>三个动作通过领域类型串联成一条流水线：
 * {@code UserInput -> Research -> Outline -> Article}。
 * 每个动作声明自己"需要什么类型、产出什么类型"，Embabel 的 GOAP 规划器
 * 据此自动推导执行顺序——无需手写编排代码。
 */
@Agent(description = "多步研究写作 Agent：调研 -> 提纲 -> 成文，执行顺序由 GOAP 规划器自动推导")
public class WritingAgent {

    @Action(description = "围绕给定主题调研出关键要点")
    public Research research(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Research.class)
                .fromPrompt("""
                        请围绕下面的主题做简要调研，输出主题和 3-5 个关键要点。
                        主题：%s
                        """.formatted(userInput.getContent()));
    }

    @Action(description = "根据调研要点设计文章提纲")
    public Outline outline(Research research, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Outline.class)
                .fromPrompt("根据以下调研结果，为文章设计一个提纲：\n" + research);
    }

    @Action(description = "按照提纲撰写文章")
    @AchievesGoal(description = "产出一篇短文")
    public Article write(Outline outline, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Article.class)
                .fromPrompt("根据以下提纲撰写一篇 300 字以内的短文：\n" + outline);
    }
}
