package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;

import java.util.ArrayList;
import java.util.List;

/**
 * 会使用工具的聊天 Agent。
 *
 * <p>两种注册工具的方式一起演示：
 * <ul>
 *   <li>{@link Tool#fromInstance(Object)}：扫描对象上的 {@code @LlmTool} 方法</li>
 *   <li>{@link Tool#create(String, String, Tool.Handler)}：用函数式接口快速定义工具</li>
 * </ul>
 * 工具由模型自主决定是否调用，必要时可多轮调用。
 */
@Agent(description = "会使用工具的聊天 Agent：计算、天气、时间等问题交给工具完成")
public class ToolChatAgent {

    @Action(description = "借助工具回答用户问题")
    @AchievesGoal(description = "返回包含工具调用结果的回答")
    public ChatReply chat(UserInput userInput, Ai ai) {
        List<Tool> tools = new ArrayList<>(Tool.fromInstance(new CalculatorTools()));
        tools.add(Tool.create(
                "echo",
                "原样返回输入的文本，可用于验证工具调用链路",
                input -> Tool.Result.text("echo:" + input)));

        String answer = ai.withDefaultLlm()
                .withTools(tools)
                .withId("tool-chat")
                .generateText("""
                        你可以调用提供的工具来回答问题，必要时进行多步推理。
                        用户问题：%s
                        """.formatted(userInput.getContent()));
        return new ChatReply(answer);
    }
}
