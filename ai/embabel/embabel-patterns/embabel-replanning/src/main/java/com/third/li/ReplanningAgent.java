package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态重规划（dynamic replanning）模式。
 *
 * <p>核心：把工具包一层"结果不达标就重规划"的装饰器
 * （{@link Tool#replanWhen} / {@code replanAlways} / {@code conditionalReplan}）：
 * <ul>
 *   <li>工具执行后会拿到它的 **artifact**</li>
 *   <li>若谓词命中（本例：artifact 是 "FAILED"），框架抛出重规划信号，把 artifact 放进黑板并**重新规划**</li>
 *   <li>动作声明 {@code canRerun = true}，因此重规划后可以再次执行 → 工具第二次调用成功</li>
 * </ul>
 *
 * <p>这正是"自主性"的体现：不需要把失败处理写死在业务代码里，而是让规划器根据结果换条路走。
 * 也可以直接在动作里抛 {@code ReplanRequestedException} 主动要求重规划；
 * 框架还提供 {@code ReplanningTools} 给工具循环用。
 */
@Agent(description = "动态重规划：工具失败时触发重规划并重试")
public class ReplanningAgent {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 统计调用次数：第一次故意失败，用于触发重规划 */
    private final AtomicInteger lookupCalls = new AtomicInteger();

    private final Tool lookupTool;

    public ReplanningAgent() {
        Tool flakyLookup = Tool.create(
                "lookup_topic",
                "查询主题资料。该服务不稳定：第一次调用会失败（用于演示重规划）",
                Tool.InputSchema.of(Tool.Parameter.string("topic", "要查询的主题")),
                input -> {
                    String topic = topicOf(input);
                    if (lookupCalls.incrementAndGet() == 1) {
                        return Tool.Result.withArtifact(
                                "查询失败：资料服务暂时不可用（将触发重规划）", "FAILED");
                    }
                    return Tool.Result.withArtifact(
                            "资料要点（%s）：1) 概念与定位 2) 关键能力 3) 适用场景".formatted(topic), "OK");
                });

        // 关键：artifact 为 FAILED 时要求重规划
        this.lookupTool = Tool.replanWhen(flakyLookup, (String artifact) -> "FAILED".equals(artifact));
    }

    @Action(canRerun = true, description = "借助工具查询资料并回答（工具失败会触发重规划）")
    @AchievesGoal(description = "产出回答")
    public Reply answer(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withTool(lookupTool)
                .withId("replanning-answer")
                .generateText("""
                        你可以调用 lookup_topic 工具查询资料。
                        该工具第一次可能失败；失败后系统会自动重规划，请再调用一次。

                        用户请求：%s
                        """.formatted(userInput.getContent()));
        return new Reply(content);
    }

    private static String topicOf(String input) {
        try {
            return MAPPER.readTree(input).path("topic").asText(input);
        } catch (Exception e) {
            return input;
        }
    }
}
