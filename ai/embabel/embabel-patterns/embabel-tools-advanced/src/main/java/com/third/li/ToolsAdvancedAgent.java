package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.api.tool.progressive.UnfoldingTool;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.loop.AutoCorrectionPolicy;

/**
 * 工具进阶：**渐进式工具 + 工具循环回调 + 找不到工具的策略**。
 *
 * <p>三件事：
 * <ol>
 *   <li><b>渐进式工具</b>：{@code UnfoldingTool.fromInstance(new OrderTools())} 把一组工具
 *       折叠成一个门面，模型按需展开（见 {@link OrderTools}）</li>
 *   <li><b>工具循环回调</b>：{@code withToolLoopInspectors(...)} / {@code withToolCallInspectors(...)}
 *       观测每轮迭代与每次工具调用（见 {@link LoggingInspector}）</li>
 *   <li><b>找不到工具的策略</b>：{@code withToolNotFoundPolicy(...)}。
 *       {@code AutoCorrectionPolicy} 会在模型把工具名写错时自动纠正（按名字相似度匹配），
 *       比直接抛异常更健壮</li>
 * </ol>
 */
@Agent(description = "工具进阶：渐进式工具 + 工具循环回调 + 工具名自动纠正")
public class ToolsAdvancedAgent {

    private final LoggingInspector inspector = new LoggingInspector();

    @Action(description = "使用渐进式工具完成订单相关请求")
    @AchievesGoal(description = "产出回答")
    public Reply chat(UserInput userInput, Ai ai) {
        Tool orderToolbox = UnfoldingTool.fromInstance(new OrderTools());

        String content = ai.withDefaultLlm()
                .withTool(orderToolbox)
                .withToolLoopInspectors(inspector)
                .withToolCallInspectors(inspector)
                .withToolNotFoundPolicy(new AutoCorrectionPolicy(2, 3, 0.7))
                .withId("tools-advanced")
                .generateText("""
                        你可以调用 order_ops 工具处理订单相关请求。
                        它是个"门面"工具：先调用它查看可用的具体操作，再按需执行。
                        写操作（取消订单）前请先与用户确认。

                        用户请求：%s
                        """.formatted(userInput.getContent()));
        return new Reply(content);
    }
}
