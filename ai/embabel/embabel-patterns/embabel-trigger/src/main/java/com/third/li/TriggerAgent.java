package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 反应式触发（reactive trigger）模式。
 *
 * <p>{@code @Action(trigger = UserInput.class)} 表示：**该动作只有在 UserInput 是黑板上最后一个对象时**
 * 才允许执行。trigger 是**额外的前置条件**，与参数类型的前置条件叠加。
 *
 * <p>为什么需要它：在**长生命周期**的进程里（例如常驻聊天机器人），黑板上会长期留着用户输入等对象。
 * 没有 trigger 时，只要动作的输入类型还在，规划器就可能反复选中它；有了 trigger，
 * 动作只在"新输入刚到达"时触发一次，避免重复执行。
 *
 * <p>本示例里 {@code classify} 是一个**不被目标需要**的工人动作（产出 {@link Category}），
 * 用来对照说明：规划器只会挑达成目标所需的动作，所以这里最终只会执行被触发约束的 {@code respond}。
 */
@Agent(description = "反应式触发：目标动作只在用户输入刚到达时执行")
public class TriggerAgent {

    /** 演示用的工人动作：把输入分类（不参与达成 Reply 目标）。 */
    @Action(description = "把用户输入归类")
    public Category classify(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Category.class)
                .fromPrompt("请用一个词概括下面问题的类别：\n" + userInput.getContent());
    }

    /**
     * 目标动作：带 trigger，仅当 {@link UserInput} 刚进入黑板时执行。
     */
    @Action(trigger = UserInput.class, description = "回答用户问题（仅在新输入刚到达时触发）")
    @AchievesGoal(description = "产出回答")
    public Reply respond(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("trigger-respond")
                .generateText("请简洁准确地回答用户问题（本回答由 trigger 约束的动作产出）：\n"
                        + userInput.getContent());
        return new Reply(content);
    }
}
