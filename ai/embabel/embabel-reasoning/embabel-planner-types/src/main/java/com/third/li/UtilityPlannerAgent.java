package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Cost;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PlannerType;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.lang.Nullable;

/**
 * UTILITY 规划器示例。
 *
 * <p>与 GOAP 不同，UTILITY 不做目标反向规划，而是在每轮挑"净价值最大"
 * （value - cost）的可用动作。动作的 value/cost 可以是静态的
 * （{@code @Action(value=..., cost=...)}），也可以用 {@link Cost} 方法在规划时动态计算
 * ——注意 {@code @Cost} 方法的所有领域参数都必须是可空的（对象不在黑板上时传 null）。
 *
 * <p>UTILITY 还不要求 Agent 必须有目标（{@code needsGoals=false}），
 * 因此很适合做常驻型的助手/聊天机器人。这里的 {@code @AchievesGoal} 只是为了能被
 * 按结果类型调用，去掉目标同样可以运行。
 */
@Agent(planner = PlannerType.UTILITY,
        description = "UTILITY 规划器示例：按 value - cost 选择动作，可无目标运行")
public class UtilityPlannerAgent {

    /**
     * 动态价值：问题越长（信息越多），越值得花算力给详细回答。
     */
    @Cost(name = "adviceValue")
    public double adviceValue(@Nullable UserInput userInput) {
        if (userInput == null) {
            return 0.5;
        }
        return userInput.getContent().length() > 15 ? 0.9 : 0.4;
    }

    @Action(description = "根据动态 value 给出回答", valueMethod = "adviceValue", cost = 0.1)
    @AchievesGoal(description = "产出回答")
    public UtilityAnswer advise(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("utility-answer")
                .generateText("请简洁准确地回答下面的问题：\n" + userInput.getContent());
        return new UtilityAnswer(content, PlannerType.UTILITY.name());
    }
}
