package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * **对照组：没有 StuckHandler 的 Agent**。
 *
 * <p>同样的"缺前提"结构（目标动作需要 {@link Analysis}，没人产出它），
 * 但这个类**没有实现** {@code StuckHandler}，所以框架只能记一条 warn 日志并把进程停在 STUCK。
 *
 * <p>框架日志（实测）：
 * <pre>
 *   Process xxx is stuck with no StuckHandler. This may or may not be an error. History (0):
 * </pre>
 */
@Agent(description = "对照组：缺前提且没有 StuckHandler，进程停在 STUCK")
public class UnhandledAgent {

    @Action(description = "根据分析要点写报告")
    @AchievesGoal(description = "产出报告")
    public UnhandledReport write(Analysis analysis, Ai ai) {
        return new UnhandledReport(ai.withDefaultLlm()
                .withId("unhandled-write")
                .generateText("根据下面的分析要点，写一段报告：\n" + analysis.points()));
    }
}
