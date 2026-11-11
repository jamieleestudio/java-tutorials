package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 两步 Agent：草稿 → 终稿。步骤间存在多个动作边界，
 * 便于观察平台在 checkpoint 处写入的进程快照。
 */
@Agent(description = "进程持久化演示：两步写作")
public class PersistenceDemoAgent {

    public record Draft(String text) {
    }

    @Action(description = "产出草稿")
    public Draft draft(UserInput userInput, Ai ai) {
        return new Draft(ai.withDefaultLlm().generateText(
                "围绕下面的主题写一段不超过 30 字的草稿：\n" + userInput.getContent()));
    }

    @Action(pre = {"draftReady"}, description = "润色成终稿")
    @AchievesGoal(description = "产出终稿")
    public String finalize(Draft draft, Ai ai) {
        return ai.withDefaultLlm().generateText(
                "润色下面的文字，只输出润色结果：\n" + draft.text());
    }

    @com.embabel.agent.api.annotation.Condition(name = "draftReady")
    public boolean draftReady(Draft draft) {
        return draft != null && draft.text() != null && !draft.text().isBlank();
    }
}
