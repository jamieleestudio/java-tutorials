package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.ai.model.Thinking;
import com.embabel.common.core.thinking.ThinkingResponse;

/**
 * 推理过程提取示例。
 *
 * <p>deepseek-flash 默认开启思考模式，模型会把推理写在独立的 reasoning 字段里。
 * Embabel 通过 {@link Thinking#withExtraction()} 打开 thinking 提取，
 * {@link PromptRunner#thinking()} 返回的 {@link ThinkingResponse} 同时包含
 * 最终答案（{@code result}）与推理过程（{@code thinkingBlocks}）。
 *
 * <p>务必先用 {@link PromptRunner#supportsThinking()} 判断，再做降级处理——
 * thinking 与 streaming 两个能力是互斥的。
 */
@Agent(description = "提取模型推理过程的示例：把思考过程与最终答案分开返回")
public class ThinkingAgent {

    @Action(description = "回答问题并提取推理过程")
    @AchievesGoal(description = "产出答案及推理过程")
    public Analysis analyze(UserInput userInput, Ai ai) {
        String prompt = """
                请解答下面的问题。先把完整的推理过程写在 <think></think> 标签里，
                然后在标签之外给出最终答案。

                问题：%s
                """.formatted(userInput.getContent());

        PromptRunner runner = ai.withLlm(
                LlmOptions.Companion.withDefaultLlm().withThinking(Thinking.withExtraction()));

        if (!runner.supportsThinking()) {
            return new Analysis(
                    runner.generateText(prompt),
                    false,
                    "",
                    "当前模型或 Provider 不支持 thinking 提取，已降级为普通回答");
        }

        ThinkingResponse<String> response = runner.thinking().generateText(prompt);
        return new Analysis(
                response.getResult(),
                response.hasThinking(),
                response.getThinkingContent(),
                null);
    }
}
