package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.api.tool.agentic.state.StateMachineTool;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * **LLM 驱动的状态机**（{@link StateMachineTool}）。
 *
 * <p>与"给模型一堆工具、让它自由发挥"不同，状态机把工具**按状态收敛**：
 * 处于 {@code NEW} 时模型只能看到 {@code validateOrder} / {@code rejectOrder}；
 * 校验通过并转移到 {@code VALIDATED} 后，才出现 {@code chargePayment}。
 * 这带来两个好处：
 * <ul>
 *   <li><b>更强的约束</b>：不可能"未支付先发货"，非法顺序在工具层面就不存在；</li>
 *   <li><b>更小的提示面</b>：每一步可选工具少，模型更不容易选错。</li>
 * </ul>
 *
 * <p>机制：{@code StateMachineTool} 本身是一个 {@link Tool}。调用它时，框架
 * 会用一个**内部 LLM 循环**在"当前状态可用的工具"里选择并执行；工具执行成功后
 * 按注册时声明的 {@code transitionsTo} 推进状态，然后重新计算可用工具集。
 *
 * <p>与相近模式的区别：
 * <ul>
 *   <li>vs {@code embabel-guardrails} / {@code embabel-secure-tools}：那两者是在**执行前/后**
 *       做校验与权限拦截；状态机是**按阶段**组织工具可见性。</li>
 *   <li>vs {@code embabel-workflows}：工作流是固定的动作图（编译期确定）；
 *       状态机是"运行时状态 + 模型自主选择"，流程走向由模型在约束内决定。</li>
 * </ul>
 */
@Agent(description = "订单处理状态机：按状态暴露不同工具并显式转移")
public class OrderWorkflowAgent {

    private static final Logger log = LoggerFactory.getLogger(OrderWorkflowAgent.class);

    @Action(description = "用状态机处理订单请求")
    @AchievesGoal(description = "完成订单处理")
    public OrderResult process(UserInput userInput) {
        StateMachineTool<OrderState> machine = buildMachine();

        Map<String, Integer> toolsPerState = new LinkedHashMap<>();
        machine.getStateToolCounts().forEach((state, count) -> toolsPerState.put(state.name(), count));
        log.info("状态机工具分布：{}，共 {} 个状态工具", toolsPerState, machine.getTotalStateTools());

        Tool.Result result = machine.call(userInput.getContent());
        String output = result instanceof Tool.Result.Text text ? text.getContent() : result.toString();
        return new OrderResult(userInput.getContent(), output, toolsPerState);
    }

    /**
     * 构建状态机：每个状态注册自己的工具，工具可声明执行后转移到哪个状态。
     *
     * <p>注意 {@code withInitialState} / {@code withGlobalTool} 以及 {@code build()}
     * 都返回**新实例**（内部是 data class 的 copy），所以要接住返回值。
     */
    private StateMachineTool<OrderState> buildMachine() {
        StateMachineTool<OrderState> machine = new StateMachineTool<>(
                "orderWorkflow",
                "订单处理状态机：校验 -> 支付 -> 发货，或在校验阶段直接拒绝",
                OrderState.class)
                .withInitialState(OrderState.NEW)
                .withMaxIterations(12)
                .withStateAwareSystemPrompt((context, input, state) -> """
                        你是订单处理助手。当前状态：%s。
                        你只能使用当前状态下可用的工具；若某个工具不存在，说明前置步骤尚未完成，
                        请先用当前可用的工具推进状态。
                        正常路径：校验 -> 支付 -> 发货；若校验发现问题，可在 NEW 状态直接拒绝。
                        全部完成后，用一句话总结订单的最终处理结果。

                        用户请求：%s
                        """.formatted(state, input));

        // 正常路径：NEW -> VALIDATED -> PAID -> SHIPPED
        // 注意：transitionsTo(...) 返回的仍是**源状态**的 builder（源码：StateBuilder(state, updated)），
        // 因此每次转移后必须显式 inState(目标状态)，否则后续工具会被注册到源状态。
        machine = machine.inState(OrderState.NEW)
                .withTool(validateOrder())
                .transitionsTo(OrderState.VALIDATED)
                .inState(OrderState.VALIDATED)
                .withTool(chargePayment())
                .transitionsTo(OrderState.PAID)
                .inState(OrderState.PAID)
                .withTool(shipOrder())
                .transitionsTo(OrderState.SHIPPED)
                .build();

        // 分支：NEW -> REJECTED（同一状态可注册多个工具，内部是追加）
        machine = machine.inState(OrderState.NEW)
                .withTool(rejectOrder())
                .transitionsTo(OrderState.REJECTED)
                .build();

        // 全局工具：所有状态都可用
        machine = machine.withGlobalTool(addNote());

        return machine;
    }

    private Tool validateOrder() {
        return Tool.create("validateOrder", "校验订单：检查库存、价格与风控", input -> {
            log.info("  [state=NEW] validateOrder");
            return Tool.Result.text("订单校验通过：库存充足、价格一致、风控无异常");
        });
    }

    private Tool chargePayment() {
        return Tool.create("chargePayment", "对已校验的订单发起扣款", input -> {
            log.info("  [state=VALIDATED] chargePayment");
            return Tool.Result.text("支付成功：已扣款 ¥299.00，交易号 TX20260916001");
        });
    }

    private Tool shipOrder() {
        return Tool.create("shipOrder", "对已支付的订单发货", input -> {
            log.info("  [state=PAID] shipOrder");
            return Tool.Result.text("已发货：顺丰速运 SF1234567890，预计次日达");
        });
    }

    private Tool rejectOrder() {
        return Tool.create("rejectOrder", "拒绝订单（库存不足或风控未通过时使用）", input -> {
            log.info("  [state=NEW] rejectOrder");
            return Tool.Result.text("订单已拒绝：库存不足，已通知客户并释放占用");
        });
    }

    private Tool addNote() {
        return Tool.create("addNote", "为订单添加处理备注（所有状态可用）", input -> {
            log.info("  [global] addNote");
            return Tool.Result.text("处理备注已记录到订单档案");
        });
    }
}
