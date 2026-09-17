package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 一个两步 Agent，用来产生 span。
 *
 * <p>不需要特殊代码——只要接上 {@link InstrumentationConfig} 里的
 * {@code AgentInstrumentation}，框架就会在进程执行、动作执行等位置自动开 span。
 */
@Agent(description = "两步 Agent：用于演示 span 采集")
public class TracedAgent {

    @Action(description = "分析")
    public Analysis analyze(UserInput userInput, Ai ai) {
        return new Analysis(ai.withDefaultLlm()
                .withId("traced-analyze")
                .generateText("请用一句话分析：\n" + userInput.getContent()));
    }

    @Action(description = "总结")
    @AchievesGoal(description = "产出总结")
    public Summary summarize(Analysis analysis, Ai ai) {
        return new Summary(ai.withDefaultLlm()
                .withId("traced-summarize")
                .generateText("请把下面这句分析总结成 30 字以内的结论：\n" + analysis.text()));
    }

    public record Analysis(String text) {
    }

    public record Summary(String text) {
    }
}
