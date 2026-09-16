package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.tools.blackboard.BlackboardTools;
import com.embabel.agent.tools.process.AgentProcessTools;

import java.util.List;

/**
 * Agent **自省工具**（agentic tools）：让模型能查看"自己当前的运行状态"。
 *
 * <p>这是"渐进式工具"最直接的应用——框架内置两组开箱即用的自省工具，
 * 它们返回的同样是**门面工具**（模型按需展开）：
 * <ul>
 *   <li>{@link BlackboardTools} —— 查看当前**黑板**（共享上下文）里的对象，
 *       适合"我手上已经有哪些信息"这类问题</li>
 *   <li>{@link AgentProcessTools} —— 查看当前**进程**的状态、目标、耗时、成本、token 用量、
 *       历史动作，适合"我做到哪一步了 / 花了多少"这类问题</li>
 * </ul>
 *
 * <p>为什么有用：Agent 出错时最常见的原因是"信息在上下文里但模型没意识到"。
 * 给它自省工具，它就能主动确认现状，而不是凭空猜。
 *
 * <p>另外还有 {@code GoalTool} / {@code AgentTool}（把一个目标 / 另一个 Agent 包装成工具，
 * 需要注入 {@code Autonomy} 与目标 / Agent 实例），用法类似，见 README。
 */
@Agent(description = "自省工具示例：Agent 可以查看自己的黑板与进程状态")
public class IntrospectiveAgent {

    @Action(description = "先自省再回答")
    @AchievesGoal(description = "产出自省报告")
    public Introspection answer(UserInput userInput, Ai ai) {
        List<Tool> tools = List.of(
                new BlackboardTools().create(),
                new AgentProcessTools().create());

        String content = ai.withDefaultLlm()
                .withTools(tools)
                .withId("introspective-answer")
                .generateText("""
                        你可以使用工具查看自己当前的**黑板内容**与**进程状态**。
                        请在回答前先自省：确认你已经掌握了哪些信息，再作答。

                        用户问题：%s
                        """.formatted(userInput.getContent()));
        return new Introspection(content);
    }
}
