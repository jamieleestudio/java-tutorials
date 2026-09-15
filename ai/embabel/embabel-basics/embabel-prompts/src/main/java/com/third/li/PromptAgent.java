package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.prompt.persona.PersonaSpec;
import com.embabel.common.ai.prompt.PromptContributor;

import java.util.Map;

/**
 * 提示词工程：**模板渲染 + 人格 + 公共提示**。
 *
 * <p>三种常用手段：
 * <ol>
 *   <li><b>Jinja 模板</b>：把提示词放进 {@code src/main/resources/prompts/<name>.jinja}，
 *       用 {@code rendering("<name>")} 渲染（变量通过 Map 传入）。模板可复用、可评审、可版本化。</li>
 *   <li><b>人格（PersonaSpec）</b>：{@code PersonaSpec.create(name, persona, voice, objective)}，
 *       作为提示词元素注入——比散落的"你是一个……"更结构化。</li>
 *   <li><b>公共提示（PromptContributor）</b>：{@code withPromptContributor(...)} 注入固定/动态片段，
 *       适合团队统一的规范（如"必须给出来源"）。</li>
 * </ol>
 *
 * <p>另外：{@code @Provided} 可以把平台/Spring 组件注入到动作方法参数（在 {@code @State} 类里尤其有用），
 * 见 {@link ProvidedDemoAgent}。
 */
@Agent(description = "提示词工程：Jinja 模板 + 人格 + 公共提示")
public class PromptAgent {

    /** 结构化人格：比一行"你是一个…"更可控。 */
    private static final PersonaSpec ANALYST = PersonaSpec.create(
            "资深架构分析师",
            "你是一位有 15 年经验的技术架构分析师",
            "严谨、直接，先给结论再给依据",
            "帮助用户把技术决策想清楚");

    /** 团队统一的公共提示。 */
    private static final PromptContributor HOUSE_RULES = PromptContributor.fixed(
            "输出必须使用中文，禁止编造事实；不确定时明确说明。");

    @Action(description = "用模板 + 人格 + 公共提示生成回答")
    @AchievesGoal(description = "产出回答")
    public Reply answer(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withPromptElements(ANALYST)
                .withPromptContributor(HOUSE_RULES)
                .rendering("answer")
                .generateText(Map.of(
                        "tone", "专业而简洁",
                        "question", userInput.getContent()));
        return new Reply(content);
    }
}
