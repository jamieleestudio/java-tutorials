package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

import java.util.ArrayList;
import java.util.List;

/**
 * **多 Agent 辩论（Multi-Agent Debate）**模式。
 *
 * <p>结构：让同一问题被**多个对立/不同视角**分别论证，再由一个"裁判"综合。
 * 相比单次调用，它能暴露单视角容易忽略的反例与风险，结论更稳。
 *
 * <p>流程：
 * <ol>
 *   <li>三位辩手分别从**支持 / 反对 / 中立务实**三个立场给出论点（三次独立 LLM 调用）</li>
 *   <li>裁判综合三方论点，给出**有取舍的结论**（指出适用条件，而不是"和稀泥"）</li>
 * </ol>
 *
 * <p>与相近模式的区别：
 * <ul>
 *   <li>vs {@code embabel-parallelization}（voting）：投票是"同一问题多票取多数"，
 *       辩论是"不同立场互相反驳"；</li>
 *   <li>vs {@code embabel-workflows}（Consensus）：共识是"多个模型给同一答案再合并"，
 *       辩论强调**对立视角**。</li>
 * </ul>
 */
@Agent(description = "多 Agent 辩论：不同立场论证后由裁判综合")
public class DebateAgent {

    private static final List<String> STANCES = List.of(
            "坚决支持，重点讲收益与机会",
            "坚决反对，重点讲风险与代价",
            "中立务实，重点讲适用条件与折中方案");

    @Action(description = "组织辩论并给出结论")
    @AchievesGoal(description = "产出辩论结论")
    public DebateResult debate(UserInput userInput, Ai ai) {
        String topic = userInput.getContent();
        List<Argument> arguments = new ArrayList<>();

        for (int i = 0; i < STANCES.size(); i++) {
            String stance = STANCES.get(i);
            String points = ai.withDefaultLlm()
                    .withId("debate-" + i)
                    .generateText("""
                            你是辩论赛的辩手，立场是：%s。
                            请针对下面的辩题给出 2~3 条有力的论点（150 字以内），不要中立化。

                            辩题：%s
                            """.formatted(stance, topic));
            arguments.add(new Argument(stance, points));
        }

        String joined = arguments.stream()
                .map(argument -> "【%s】\n%s".formatted(argument.stance(), argument.points()))
                .reduce("", (a, b) -> a + b + "\n");

        String conclusion = ai.withDefaultLlm()
                .withId("debate-judge")
                .generateText("""
                        你是辩论裁判。请综合下面的三方论点，给出**有取舍的结论**：
                        明确什么条件下该做、什么条件下不该做，不要含糊其辞。

                        辩题：%s
                        各方论点：
                        %s
                        """.formatted(topic, joined));

        return new DebateResult(topic, arguments, conclusion);
    }
}
