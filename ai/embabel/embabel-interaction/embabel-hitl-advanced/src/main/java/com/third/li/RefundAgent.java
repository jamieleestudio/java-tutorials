package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.hitl.ConditionalAwaitingTool;
import com.embabel.agent.core.hitl.TypeRequest;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * **工具级 HITL**：把"向用户要输入"下沉到**工具调用点**，而不是单独开一个动作去等。
 *
 * <p>核心是 {@link ConditionalAwaitingTool} + {@code AwaitDecider}：
 * 每次调用工具前先问 decider"要不要等用户"，返回 {@code null} 就直接执行，
 * 返回一个 {@code Awaitable} 就抛 {@code AwaitableResponseException} 让进程进入 WAITING。
 *
 * <p>本模块的 decider 体现两种"只在需要时才问"：
 * <ol>
 *   <li><b>按业务阈值</b>：金额 ≤ {@value #AUTO_APPROVE_LIMIT} 直接执行，不打扰用户</li>
 *   <li><b>按已有信息</b>：已经提供过 {@link RefundAccount} 就不再问第二次</li>
 * </ol>
 * 第 2 条是**必须**的：{@code AwaitableResponseException} 的处理路径没有去重
 * （见 README 的"⚠️ 为什么不用 TypeRequestingTool"），decider 自己不守卫就会反复进入等待。
 *
 * <p>与相近机制的区别：
 * <ul>
 *   <li>vs {@code embabel-hitl}：那里用 {@code WaitFor.confirmation/formSubmission}
 *       在**动作内部**等输入；这里在**工具调用点**等，粒度更细，且能按条件决定要不要问。</li>
 *   <li>vs {@code embabel-secure-tools}：那里是"危险操作前必须确认"（策略固定）；
 *       这里是"按运行时条件决定是否索要强类型输入"。</li>
 * </ul>
 */
@Agent(description = "工具级 HITL：退款工具在必要时才向用户索取强类型输入")
public class RefundAgent {

    private static final Logger log = LoggerFactory.getLogger(RefundAgent.class);

    /** 自动审批上限：超过它才向用户索要账号信息。 */
    static final double AUTO_APPROVE_LIMIT = 1000.0;

    private static final Pattern ORDER_ID = Pattern.compile("订单\\s*([A-Za-z0-9_-]+)");
    private static final Pattern AMOUNT = Pattern.compile("金额\\s*([0-9]+(?:\\.[0-9]+)?)");

    // canRerun = true 是工具级 HITL 的关键：动作在等待时已被记为"执行过"，
    // 恢复后框架**不会**自动重跑它（默认 canRerun=false），
    // 而 TypeRequest 补进来的 RefundAccount 并不满足目标 RefundOutcome → 会直接 STUCK。
    @Action(description = "处理退款（必要时向用户索取账号信息）", canRerun = true)
    @AchievesGoal(description = "产出退款结果")
    public RefundOutcome handle(UserInput userInput) {
        String text = userInput.getContent();
        String orderId = firstMatch(ORDER_ID, text, "A1001");
        double amount = Double.parseDouble(firstMatch(AMOUNT, text, "0"));

        Tool refund = Tool.create("issueRefund", "发起退款", args ->
                Tool.Result.text("退款已发起：%s".formatted(args)));

        Tool guarded = new ConditionalAwaitingTool(refund, awaitContext -> {
            AgentProcess process = awaitContext.getAgentProcess();
            if (amount <= AUTO_APPROVE_LIMIT) {
                log.info("金额 {} ≤ {}，自动审批，不打扰用户", amount, AUTO_APPROVE_LIMIT);
                return null;
            }
            if (process.last(RefundAccount.class) != null) {
                log.info("用户已提供退款账号，直接执行");
                return null;
            }
            log.info("金额 {} > {}，向用户索取退款账号", amount, AUTO_APPROVE_LIMIT);
            return new TypeRequest<>(
                    RefundAccount.class,
                    "退款金额 %.0f 超过自动审批上限 %.0f，请提供退款账号信息"
                            .formatted(amount, AUTO_APPROVE_LIMIT));
        });

        AgentProcess process = AgentProcess.get();
        RefundAccount account = process == null ? null : process.last(RefundAccount.class);

        Tool.Result result = guarded.call("""
                {"orderId":"%s","amount":%s,"account":"%s"}"""
                .formatted(orderId, amount, account == null ? "自动审批-默认账号" : account.account()));
        String content = result instanceof Tool.Result.Text text1 ? text1.getContent() : result.toString();

        return new RefundOutcome(orderId, amount, account != null, content);
    }

    private String firstMatch(Pattern pattern, String text, String fallback) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : fallback;
    }
}
