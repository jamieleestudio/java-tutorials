package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.core.hitl.WaitFor;
import com.embabel.agent.domain.io.UserInput;

/**
 * 表单型人机协同 Agent。
 *
 * <p>{@link WaitFor#formSubmission(String, Class)} 会根据 {@link ContactInfo}
 * 自动生成表单并暂停流程；用户提交后表单内容被绑定成对象进入黑板。
 *
 * <p>注意：表单动作的返回类型必须与绑定类型一致（这里都是 {@link ContactInfo}），
 * 再由后续动作消费该对象产出最终结果。
 */
@Agent(description = "表单型 HITL：填写联系信息后生成跟进邮件")
public class ContactFormAgent {

    @Action(description = "通过表单收集联系信息")
    public ContactInfo collect(UserInput userInput) {
        return WaitFor.formSubmission("请填写联系信息", ContactInfo.class);
    }

    @Action(description = "根据联系信息生成跟进邮件")
    @AchievesGoal(description = "产出个性化回复")
    public ContactReply reply(ContactInfo contact, UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("contact-reply")
                .generateText("根据下面的用户信息写一段简短的跟进邮件：\n姓名：%s\n邮箱：%s\n需求：%s"
                        .formatted(contact.name(), contact.email(), userInput.getContent()));
        return new ContactReply(content);
    }
}
