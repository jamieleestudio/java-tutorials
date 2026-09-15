package com.third.li;

import com.embabel.agent.api.annotation.LlmTool;

/**
 * 把"远端 A2A 智能体"包装成工具，供本地 Agent 调用。
 */
public class RemoteA2ATools {

    private final A2AClient client;

    public RemoteA2ATools(A2AClient client) {
        this.client = client;
    }

    @LlmTool(description = "把问题转交给远端 A2A 智能体，并返回它的回答")
    public String askRemoteAgent(
            @LlmTool.Param(description = "要问远端智能体的问题") String question) {
        try {
            return client.send(question);
        } catch (Exception e) {
            return "调用远端智能体失败：" + e.getMessage();
        }
    }
}
