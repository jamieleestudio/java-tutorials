package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;

/**
 * 本地 Agent：把远端 A2A 智能体当作**工具**来用（Agent-to-Agent 协作）。
 *
 * <p>与直接调用 A2A 客户端的区别：这里由模型决定何时调用 `askRemoteAgent`，
 * 并可把远端返回的结果再加工后回复用户——这正是 A2A 想解决的"智能体互相协作"。
 */
@Agent(description = "本地 Agent：把问题委派给远端 A2A 智能体并整理回复")
public class LocalAgent {

    private final A2AClient client;

    public LocalAgent(A2AClient client) {
        this.client = client;
    }

    @Action(description = "通过远端 A2A 智能体回答")
    @AchievesGoal(description = "产出回答")
    public DelegatedReply delegate(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withTools(Tool.fromInstance(new RemoteA2ATools(client)))
                .withId("a2a-delegate")
                .generateText("""
                        你可以调用 askRemoteAgent 工具，把问题转交给远端 A2A 智能体。
                        请把它的回答整理成简洁的中文回复。

                        用户请求：%s
                        """.formatted(userInput.getContent()));
        return new DelegatedReply(content);
    }
}
