package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Condition;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

import java.util.Locale;

/**
 * Routing（路由）模式。
 *
 * <p>对应 Anthropic《Building Effective Agents》里的 *Routing*：先**分类**输入，
 * 再交给**专门的后续处理**（不同提示词/工具/模型）。好处是"关注点分离"，
 * 避免用一套提示词硬扛所有输入。
 *
 * <p>Embabel 的实现方式很自然：
 * <ol>
 *   <li>一个动作做分类，产出 {@link Route}；并声明
 *       {@code post = {"isRefund","isTech","isGeneral"}}（"分类后可能让这些条件成立"）</li>
 *   <li>用 {@link Condition} 声明"是否退款/技术/一般咨询"——规划器会**真正求值**它们</li>
 *   <li>三个专门处理器各自 {@code @AchievesGoal} + {@code pre = 对应条件}——
 *       只有条件成立的那一个能被选中</li>
 * </ol>
 *
 * <p>这与 {@code embabel-multi-goal} 的区别：那里是**排序器**在多个目标间选择；
 * 这里是**条件**精确决定走哪条通道，语义更确定。
 */
@Agent(description = "路由模式：分类后交给专门的处理器")
public class RoutingAgent {

    @Action(post = {"isRefund", "isTech", "isGeneral"}, description = "把客服请求分类")
    public Route classify(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Route.class)
                .fromPrompt("""
                        请把下面的客服请求分类，kind 必须是 REFUND、TECH、GENERAL 之一：
                        - REFUND：退款、退货、赔偿相关
                        - TECH：技术故障、报错、使用方法相关
                        - GENERAL：其它一般咨询

                        请求：%s
                        """.formatted(userInput.getContent()));
    }

    @Condition(name = "isRefund")
    public boolean isRefund(Route route) {
        return matches(route, "REFUND", "退款", "退货");
    }

    @Condition(name = "isTech")
    public boolean isTech(Route route) {
        return matches(route, "TECH", "技术", "故障", "报错");
    }

    @Condition(name = "isGeneral")
    public boolean isGeneral(Route route) {
        return matches(route, "GENERAL", "一般", "咨询", "其他", "其它");
    }

    @Action(pre = {"isRefund"}, description = "退款通道")
    @AchievesGoal(description = "产出回复")
    public Reply refund(Route route, UserInput userInput, Ai ai) {
        return new Reply("REFUND", ai.withDefaultLlm().withId("routing-refund").generateText(
                "你是退款专员。请按退款政策回答，并说明所需材料与时效。\n请求："
                        + userInput.getContent()));
    }

    @Action(pre = {"isTech"}, description = "技术通道")
    @AchievesGoal(description = "产出回复")
    public Reply tech(Route route, UserInput userInput, Ai ai) {
        return new Reply("TECH", ai.withDefaultLlm().withId("routing-tech").generateText(
                "你是技术支持工程师。请给出排查步骤与可能的解决方案。\n问题："
                        + userInput.getContent()));
    }

    @Action(pre = {"isGeneral"}, description = "一般咨询通道")
    @AchievesGoal(description = "产出回复")
    public Reply general(Route route, UserInput userInput, Ai ai) {
        return new Reply("GENERAL", ai.withDefaultLlm().withId("routing-general").generateText(
                "你是客服代表。请友好、简洁地解答用户的咨询。\n咨询："
                        + userInput.getContent()));
    }

    private boolean matches(Route route, String... keywords) {
        String kind = route.kind() == null ? "" : route.kind().toUpperCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (kind.contains(keyword.toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
