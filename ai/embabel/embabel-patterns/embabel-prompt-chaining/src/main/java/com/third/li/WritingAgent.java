package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Condition;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * Prompt chaining（链式提示）+ **关卡（gate）** 模式。
 *
 * <p>对应 Anthropic《Building Effective Agents》里的 *Prompt chaining*：
 * 把任务拆成固定顺序的步骤，每步处理上一步的输出；中间可插入**程序化检查（gate）**，
 * 确认流程还在正轨上，不通过就停止（而不是继续跑偏）。
 *
 * <p>链：{@code UserInput -> Outline -> GateResult -> Article}
 *
 * <p>关卡实现的关键：用 {@link Condition} 声明"提纲已通过校验"，让关卡动作
 * {@code post = "outlineAccepted"}（表示"这个动作可能让该条件成立"），
 * 再让成文动作 {@code pre = "outlineAccepted"}。规划器会先跑关卡动作，
 * 然后**真正求值**该条件：成立才继续；不成立则找不到通往 {@link Article} 的路径，
 * 流程停止（控制器据此返回"被关卡拦截"）。
 *
 * <p>适用场景（文章原话）：任务能干净地拆成固定子任务、愿意用延迟换准确率。
 */
@Agent(description = "链式提示 + 关卡：提纲 -> 校验(gate) -> 成文")
public class WritingAgent {

    @Action(description = "第一步：生成文章提纲")
    public Outline outline(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Outline.class)
                .fromPrompt("请为下面的主题生成一个文章提纲（标题 + 3~5 个小节）：\n" + userInput.getContent());
    }

    @Action(post = {"outlineAccepted"}, description = "第二步：程序化关卡，检查提纲是否可用")
    public GateResult check(Outline outline, UserInput userInput, Ai ai) {
        boolean ok = ai.withDefaultLlm()
                .evaluateCondition(
                        "该提纲是否紧扣主题、结构清晰、且小节数量在 3 到 5 个之间？",
                        "主题：" + userInput.getContent() + "\n提纲：" + outline,
                        0.8);
        return new GateResult(
                outline,
                ok,
                ok ? "提纲通过校验" : "提纲未通过校验（主题偏离或结构不清），链条停止");
    }

    /** 关卡条件：只有校验通过才允许进入下一步。 */
    @Condition(name = "outlineAccepted")
    public boolean outlineAccepted(GateResult gateResult) {
        return gateResult.passed();
    }

    @Action(pre = {"outlineAccepted"}, description = "第三步：按提纲成文")
    @AchievesGoal(description = "产出文章")
    public Article write(GateResult gateResult, UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("chaining-write")
                .generateText("""
                        请严格按照提纲撰写文章（300 字以内）。

                        主题：%s
                        提纲：%s
                        """.formatted(userInput.getContent(), gateResult.outline()));
        return new Article(gateResult.outline().title(), content);
    }
}
