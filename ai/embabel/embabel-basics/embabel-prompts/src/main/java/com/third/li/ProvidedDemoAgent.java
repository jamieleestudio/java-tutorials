package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Provided;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * {@code @Provided} 演示：把**平台/Spring 组件**注入动作方法参数。
 *
 * <p>注意参数上的 {@link Provided}：它告诉框架"这个参数不从黑板解析，而是由平台提供"
 * （Spring 环境下按类型从容器取）。这在 {@code @State} 类里尤其有用——
 * 状态类通常是静态/数据类，拿不到外层组件的依赖。
 */
@Agent(description = "@Provided 注入演示：动作方法直接拿到 Spring 组件")
public class ProvidedDemoAgent {

    @Action(description = "使用注入的格式化组件输出")
    @AchievesGoal(description = "产出带格式的回答")
    public FormattedReply answer(UserInput userInput, @Provided ReplyFormatter formatter, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("provided-answer")
                .generateText("请简洁回答：\n" + userInput.getContent());
        return new FormattedReply(formatter.wrap(content), formatter.formatName());
    }
}
