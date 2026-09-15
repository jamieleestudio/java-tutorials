package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 多模型协作示例：按角色选择模型。
 *
 * <p>在 `embabel.models.llms` 里把角色映射到模型名：
 * <pre>
 * embabel:
 *   models:
 *     llms:
 *       fast: deepseek-flash
 *       deep: deepseek-v4-pro
 * </pre>
 * 之后用 {@code ai.withLlmByRole("fast")} 就能拿到对应模型——适合"便宜模型做粗活、
 * 贵模型做终稿"的成本优化，也便于按团队/租户切换模型。
 *
 * <p>相关 API：{@code ai.withLlm("模型名")}、{@code ai.withAutoLlm()}、
 * {@code ai.withFirstAvailableLlmOf(...)}（见 {@link FallbackAgent}）。
 */
@Agent(description = "多模型示例：fast 角色列要点，deep 角色写终稿")
public class MultiModelAgent {

    @Action(description = "用 fast 角色列要点、deep 角色写终稿")
    @AchievesGoal(description = "产出回答")
    public Answer answer(UserInput userInput, Ai ai) {
        String points = ai.withLlmByRole("fast")
                .withId("multi-model-points")
                .generateText("请用 3 个要点列出回答思路：\n" + userInput.getContent());

        String draft = ai.withLlmByRole("deep")
                .withId("multi-model-draft")
                .generateText("""
                        请根据下面的要点，写一段完整、准确的中文回答。

                        问题：%s
                        要点：%s
                        """.formatted(userInput.getContent(), points));

        return new Answer(draft, "fast(deepseek-flash) -> deep(deepseek-v4-pro)");
    }
}
