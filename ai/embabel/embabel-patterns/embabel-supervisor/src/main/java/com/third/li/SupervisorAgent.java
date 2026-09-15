package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PlannerType;
import com.embabel.agent.domain.io.UserInput;

/**
 * Supervisor（主管）模式。
 *
 * <p>{@code @Agent(planner = PlannerType.SUPERVISOR)} 会把**除目标动作外**的动作暴露成"工具"，
 * 然后由一个 **LLM 主管**在循环里决定"下一步调用哪个动作"（类似 LangGraph 的 supervisor）：
 * <ol>
 *   <li>框架把每个动作包成工具；已在黑板上的输入会被"柯里化"掉，工具参数只剩缺的那些</li>
 *   <li>主管 LLM 看到当前已收集的产出与可用动作，决定调哪一个</li>
 *   <li>目标动作的输入齐备后，自动执行并产出目标类型（最多 10 轮，防死循环）</li>
 * </ol>
 *
 * <p><b>约束</b>：SUPERVISOR 要求该 Agent **有且仅有一个** {@code @AchievesGoal} 动作，
 * 否则启动时校验失败。这里：{@code gatherFacts} → {@code makeOutline} 是工人动作，
 * {@code write} 是唯一目标动作。
 */
@Agent(planner = PlannerType.SUPERVISOR,
        description = "主管模式：LLM 作为主管按需调度动作，最终产出回答")
public class SupervisorAgent {

    @Action(description = "收集关于主题的关键事实")
    public Facts gatherFacts(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Facts.class)
                .fromPrompt("请围绕下面的主题收集 3 条关键事实：\n" + userInput.getContent());
    }

    @Action(description = "根据事实列出回答提纲")
    public Outline makeOutline(Facts facts, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Outline.class)
                .fromPrompt("根据以下事实，为回答列出提纲：\n" + facts);
    }

    @Action(description = "综合产出最终回答")
    @AchievesGoal(description = "产出最终回答")
    public FinalAnswer write(Outline outline, UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("supervisor-write")
                .generateText("""
                        请根据提纲回答问题，输出一段完整、准确的中文回答。

                        问题：%s
                        提纲：%s
                        """.formatted(userInput.getContent(), outline));
        return new FinalAnswer(content);
    }
}
