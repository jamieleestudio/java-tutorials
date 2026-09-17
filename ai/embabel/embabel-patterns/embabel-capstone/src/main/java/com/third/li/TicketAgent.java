package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.reference.LlmReference;
import com.embabel.agent.api.reference.LiteralText;
import com.embabel.agent.core.hitl.WaitFor;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * **端到端综合示例**：把六个模块教的东西串成一个"像产品的工单处理 Agent"。
 *
 * <p>它刻意**不引入任何新概念**，价值在于让你看到这些能力**如何组合**：
 *
 * <table border="1">
 *   <tr><th>阶段</th><th>用到的能力</th><th>单独讲它的模块</th></tr>
 *   <tr><td>1. 输入护栏</td><td>提示词注入 / 敏感内容筛查</td><td>{@code embabel-guardrails}</td></tr>
 *   <tr><td>2. 政策检索</td><td>{@code LlmReference} + {@code LiteralText}</td><td>{@code embabel-references}</td></tr>
 *   <tr><td>3. 查订单</td><td>{@code @LlmTool} 工具（返回领域对象）</td><td>{@code embabel-tools} / {@code embabel-tool-chaining}</td></tr>
 *   <tr><td>4. 人工确认</td><td>{@code WaitFor.confirmation}（超过阈值才问）</td><td>{@code embabel-hitl} / {@code embabel-hitl-advanced}</td></tr>
 *   <tr><td>5. 预算约束</td><td>{@code Budget} + {@code ProcessControl}</td><td>{@code embabel-budget}</td></tr>
 *   <tr><td>6. 成本观测</td><td>{@code AgenticEventListener}</td><td>{@code embabel-observability}</td></tr>
 * </table>
 *
 * <p>注意第 4 步的设计：**不是每个工单都弹确认框**，而是"金额超过阈值才问"——
 * 这是 HITL 在产品里唯一可接受的做法（否则用户会被确认框淹没）。
 */
@Agent(description = "端到端综合示例：工单处理（护栏 → 政策检索 → 查订单 → 人工确认）")
public class TicketAgent {

    private static final Logger log = LoggerFactory.getLogger(TicketAgent.class);

    /** 超过这个金额才需要人工确认。 */
    static final double CONFIRM_THRESHOLD = 500.0;

    /** 退款政策（真实项目里放在外部知识库，用 LlmReference / 向量检索加载）。 */
    private static final String POLICY = """
            退款政策（2026 版）
            1. 已支付订单在 15 天内可申请退款；超过 15 天需人工审批。
            2. 商品破损、错发、缺件属于卖家责任，全额退款并免运费。
            3. 单笔退款金额超过 500 元必须人工确认后方可执行。
            4. 虚拟商品一经使用不支持退款。
            """;

    /** 简易输入护栏：命中即拒。生产请用 UserInputGuardRail（见 embabel-guardrails）。 */
    private static final List<String> INJECTION_MARKERS = List.of(
            "忽略之前的指令", "ignore previous", "system prompt", "泄露提示词");

    private static final Pattern ORDER_ID = Pattern.compile("([A-Z]\\d{3,})");
    private static final Pattern AMOUNT = Pattern.compile("金额\\s*([0-9]+(?:\\.[0-9]+)?)");

    private final OrderTools orderTools;

    public TicketAgent(OrderTools orderTools) {
        this.orderTools = orderTools;
    }

    @Action(description = "处理工单（高风险动作需要人工确认）")
    @AchievesGoal(description = "产出工单处理结果")
    public TicketResult handle(UserInput userInput, Ai ai) {
        String text = userInput.getContent();
        List<String> stages = new ArrayList<>();

        // ---- 阶段 1：输入护栏 ----
        String violation = screen(text);
        if (violation != null) {
            stages.add("guardrail:blocked");
            log.info("护栏拦截：{}", violation);
            return new TicketResult("REJECTED", "输入被护栏拦截：" + violation, null, 0, stages);
        }
        stages.add("guardrail:passed");

        // ---- 阶段 2：政策检索（作为 reference 注入，而不是塞进系统提示词）----
        LlmReference policy = new LiteralText("退款政策", "客服退款与补偿政策条款", POLICY);
        stages.add("reference:loaded");

        // ---- 阶段 3：工具（模型自主调用 queryOrder）----
        String analysis = ai.withDefaultLlm()
                .withReference(policy)
                .withToolObject(orderTools)
                .withId("capstone-analyze")
                .generateText("""
                        你是客服助手。请：
                        1) 先查阅「退款政策」的相关条款；
                        2) 调用 queryOrder 查询工单里提到的订单；
                        3) 给出处理建议：能否退款、金额多少、依据哪一条条款。

                        工单内容：%s
                        """.formatted(text));
        stages.add("tools:queryOrder");

        String orderId = firstMatch(ORDER_ID, text, "UNKNOWN");
        double amount = Double.parseDouble(firstMatch(AMOUNT, text, "0"));
        stages.add("policy:evaluated");

        // ---- 阶段 4：高风险才人工确认 ----
        if (amount > CONFIRM_THRESHOLD) {
            stages.add("hitl:requested");
            log.info("金额 {} > 阈值 {}，请求人工确认", amount, CONFIRM_THRESHOLD);
            // WaitFor.confirmation 返回 payload 本身；payload 类型 == 目标类型，
            // 所以确认后把 payload 放回黑板即视为达成目标（见 embabel-hitl 的机制说明）
            return WaitFor.confirmation(
                    new TicketResult("PENDING_CONFIRMATION", analysis, orderId, amount, stages),
                    "退款金额 %.0f 元超过 %.0f 元阈值，是否批准？".formatted(amount, CONFIRM_THRESHOLD));
        }

        stages.add("auto:approved");
        return new TicketResult("AUTO_APPROVED", analysis, orderId, amount, stages);
    }

    private String screen(String text) {
        String lower = text.toLowerCase();
        for (String marker : INJECTION_MARKERS) {
            if (lower.contains(marker.toLowerCase())) {
                return "疑似提示词注入（命中：" + marker + "）";
            }
        }
        if (text.length() > 2000) {
            return "输入过长（>2000 字符）";
        }
        return null;
    }

    private String firstMatch(Pattern pattern, String text, String fallback) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : fallback;
    }
}
