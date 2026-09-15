package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Autonomous agent（自主 Agent）模式。
 *
 * <p>对应 Anthropic《Building Effective Agents》里的 *Agents*：**LLM 在循环里使用工具**，
 * 每步都从环境（工具返回）获得**真实反馈**来判断进展；遇到错误能自行恢复；
 * 并有**停止条件**以防失控。
 *
 * <p>本例演示三件事：
 * <ol>
 *   <li><b>工具循环 + 真实反馈</b>：模型可多次调用工具（计算器、知识库），
 *       每次的结果都会回到模型，由它决定下一步</li>
 *   <li><b>错误恢复</b>：知识库工具**首次调用故意返回错误**（{@code Tool.Result.error}），
 *       模型看到错误后会改变策略重试——这正是文章强调的"recovering from errors"</li>
 *   <li><b>停止条件</b>：工具循环有最大轮数上限（框架默认 20），避免无限循环</li>
 * </ol>
 *
 * <p>生产上还需要：**人工检查点**（见 {@code embabel-hitl} 的 `WaitFor.confirmation`）、
 * **护栏**（见 {@code embabel-guardrails}）、**沙箱化的工具**（见 {@code embabel-file-tools}）。
 */
@Agent(description = "自主 Agent：工具循环 + 环境反馈 + 错误恢复 + 停止条件")
public class AutonomousAgent {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AtomicInteger knowledgeCalls = new AtomicInteger();

    @Action(description = "自主完成任务：可多次调用工具，遇错自行恢复")
    @AchievesGoal(description = "产出回答")
    public Answer solve(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withTools(List.of(calculator(), knowledgeLookup()))
                .withId("autonomous-solve")
                .generateText("""
                        你是一个自主智能体：可以调用工具，并根据工具返回的**真实结果**决定下一步。
                        规则：
                        - 需要计算时调用 calculator；
                        - 需要内部资料时调用 knowledge_lookup；
                        - 如果工具返回错误，请换一种方式重试（换个查询、或先算清楚再查）；
                        - 拿到足够信息后给出最终回答。

                        用户任务：%s
                        """.formatted(userInput.getContent()));
        return new Answer(content);
    }

    private Tool calculator() {
        return Tool.create(
                "calculator",
                "做四则运算。参数：a、b、op（+ - * /）",
                Tool.InputSchema.of(
                        // 注意：Kotlin 的 Parameter.double(...) 在 Java 里不可调用（double 是关键字），
                        // 因此这里用构造函数 + ParameterType.NUMBER
                        new Tool.Parameter("a", Tool.ParameterType.NUMBER, "第一个数"),
                        new Tool.Parameter("b", Tool.ParameterType.NUMBER, "第二个数"),
                        Tool.Parameter.string("op", "运算符：+、-、*、/")),
                input -> {
                    try {
                        JsonNode node = MAPPER.readTree(input);
                        double a = node.path("a").asDouble();
                        double b = node.path("b").asDouble();
                        String op = node.path("op").asText("+");
                        double result = switch (op) {
                            case "-" -> a - b;
                            case "*" -> a * b;
                            case "/" -> b == 0 ? Double.NaN : a / b;
                            default -> a + b;
                        };
                        return Tool.Result.text("%s %s %s = %s".formatted(a, op, b, result));
                    } catch (Exception e) {
                        return Tool.Result.error("参数解析失败：" + e.getMessage(), e);
                    }
                });
    }

    /** 首次调用故意失败，用于演示"错误恢复"。 */
    private Tool knowledgeLookup() {
        return Tool.create(
                "knowledge_lookup",
                "查询内部知识库。注意：该服务不稳定，首次调用可能返回错误",
                Tool.InputSchema.of(Tool.Parameter.string("query", "查询内容")),
                input -> {
                    if (knowledgeCalls.incrementAndGet() == 1) {
                        return Tool.Result.error("知识库暂时不可用（错误码 503），请稍后重试或换个问法。", null);
                    }
                    return Tool.Result.text(
                            "知识库结果：内部文档指出，引入 Agent 框架前应先明确目标边界、工具接口与权限模型。");
                });
    }
}
