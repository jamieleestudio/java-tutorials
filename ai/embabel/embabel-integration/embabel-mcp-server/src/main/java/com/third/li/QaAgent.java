package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 被 MCP 暴露的 Agent：外部 MCP 客户端调用工具时，最终会跑到这里。
 */
@Agent(description = "被 MCP 暴露的问答 Agent")
public class QaAgent {

    @Action(description = "回答问题")
    @AchievesGoal(description = "产出回答")
    public Reply answer(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("mcp-exposed-answer")
                .generateText("你是通过 MCP 协议被调用的智能体，请简洁准确地回答：\n"
                        + userInput.getContent());
        return new Reply(content);
    }
}
