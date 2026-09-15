package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;

/**
 * 安全实践：**最小权限工具 + PII 护栏 + 敏感操作显式确认**。
 *
 * <p>三层防护：
 * <ol>
 *   <li><b>最小权限</b>：只挂 {@link ReadOnlyOrderTools}（只读、已脱敏），
 *       导出/修改类能力根本不暴露给模型</li>
 *   <li><b>PII 护栏</b>：{@link PiiInputGuardRail} 拦输入、{@link PiiOutputGuardRail} 拦输出，
 *       命中 CRITICAL 直接阻断</li>
 *   <li><b>敏感操作确认</b>：真正需要写操作时走人工审批（{@code embabel-hitl}），
 *       或在应用层要求显式 {@code confirm=true}（见控制器）</li>
 * </ol>
 */
@Agent(description = "安全实践：最小权限工具 + PII 护栏")
public class SecureAgent {

    @Action(description = "在护栏与最小权限下回答")
    @AchievesGoal(description = "产出回答")
    public Reply chat(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withGuardRails(new PiiInputGuardRail(), new PiiOutputGuardRail())
                .withTools(Tool.fromInstance(new ReadOnlyOrderTools()))
                .withId("secure-chat")
                .generateText("""
                        你是客服助手，只有**只读**的订单查询工具。
                        规则：
                        - 不得索取或输出任何个人敏感信息（身份证/手机号/邮箱）；
                        - 用户要求导出、修改、取消等写操作时，明确说明需要人工审批，不能执行；
                        - 回答简洁。

                        用户请求：%s
                        """.formatted(userInput.getContent()));
        return new Reply(content);
    }
}
