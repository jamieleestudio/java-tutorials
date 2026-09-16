package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * **工具链式展开（tool chaining）**。
 *
 * <p>核心是一行：{@code withToolChainingFrom(Order.class)}。
 * 它的含义是——"一旦黑板上出现了 `Order` 实例，就把 `Order` 上的 `@LlmTool` 方法也作为工具暴露出来"。
 *
 * <p>执行过程（实测日志可见）：
 * <ol>
 *   <li>模型先看到 `searchOrder`（第一批工具）</li>
 *   <li>调用 `searchOrder("A1001")` → 返回 `Order` 对象 → 被 artifact 收集器落到黑板</li>
 *   <li>因为黑板上出现了 `Order`，`applyDiscount` / `currentStatus` **才**出现在工具列表里</li>
 *   <li>模型继续调用 `applyDiscount(0.9)` 完成用户请求</li>
 * </ol>
 *
 * <p>为什么有用：工具很多时不必一次性全塞进提示词。**领域对象出现 = 解锁条件**，
 * 这与 {@code embabel-tools-advanced} 的"渐进式工具（按 category 手动展开）"互补——
 * 那里靠模型主动展开门面，这里靠数据驱动的自动解锁。
 *
 * <p>底层机制就是 artifacts：
 * {@code Tool.publishToBlackboard} / {@code Tool.sinkArtifacts} / {@code ArtifactSink}
 * 是它的显式 API（见本模块的 {@code /artifacts/sink} 端点）。
 */
@Agent(description = "工具链式展开：工具产出领域对象后，该对象的专属工具自动解锁")
public class ToolChainingAgent {

    @Action(description = "检索订单并按需操作")
    @AchievesGoal(description = "产出回答")
    public Reply chat(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                // 第一批工具：只有 searchOrder
                .withToolObject(new OrderTools())
                // 解锁条件：黑板上出现 Order 实例后，Order 的 @LlmTool 方法变为可用
                .withToolChainingFrom(Order.class)
                .withId("tool-chaining")
                .generateText("""
                        你可以先用 searchOrder 查询订单。
                        查到订单之后，该订单的专属操作（applyDiscount / currentStatus）会自动变得可用，
                        请继续调用它们完成用户的请求。

                        用户请求：%s
                        """.formatted(userInput.getContent()));
        return new Reply(content);
    }
}
