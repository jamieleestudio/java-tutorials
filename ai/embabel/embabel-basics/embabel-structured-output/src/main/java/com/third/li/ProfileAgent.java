package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

import java.util.List;

/**
 * 结构化输出 Agent：把自由文本转成强类型对象。
 *
 * <p>{@code creating(Profile.class)} 让 LLM 返回可直接映射为 {@link Profile} 的内容；
 * {@code withExample} 提供示例提升稳定性，{@code withValidation} 开启结果校验。
 */
@Agent(description = "从自由文本中抽取结构化个人档案")
public class ProfileAgent {

    @Action(description = "抽取结构化的个人档案")
    @AchievesGoal(description = "产出结构化档案")
    public Profile extract(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Profile.class)
                .withExample("示例", new Profile("张三", 30, List.of("Java", "Spring"), "后端工程师"))
                .withValidation(true)
                .fromPrompt("请从下面的自我介绍中抽取结构化信息：\n" + userInput.getContent());
    }
}
