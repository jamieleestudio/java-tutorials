package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 模型回退示例。
 *
 * <p>{@code ai.withFirstAvailableLlmOf(...)} 会按顺序尝试候选模型：
 * 找不到的会被跳过（日志里会打印 "Requested LLM 'xxx' not found"），
 * 用第一个可用的模型执行。适合"优先用某模型，不可用时降级"的场景。
 */
@Agent(description = "模型回退示例：按顺序尝试候选模型")
public class FallbackAgent {

    @Action(description = "使用第一个可用的候选模型回答")
    @AchievesGoal(description = "产出回答")
    public FallbackAnswer answer(UserInput userInput, Ai ai) {
        String content = ai.withFirstAvailableLlmOf("gpt-5.4", "deepseek-flash")
                .withId("fallback-answer")
                .generateText("请简洁准确地回答：\n" + userInput.getContent());
        return new FallbackAnswer(content, "gpt-5.4（未注册，跳过） -> deepseek-flash");
    }
}
