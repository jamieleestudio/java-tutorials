package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * 多租户 Agent：按**租户等级**选择模型（basic → 便宜模型，pro → 强模型）。
 *
 * <p>这是 BYOK/多租户的"模型侧"基础：把"用哪个模型"从代码里拿出来，变成配置 + 运行时选择。
 * 真正的 BYOK（每个用户带自己的 API Key）在此基础上把"角色→模型"换成"用户 Key → 模型"，
 * 见 README 的说明与代码片段。
 */
@Agent(description = "多租户 Agent：按租户等级路由到不同模型")
public class TenantAgent {

    @Action(description = "按租户等级选择模型并回答")
    @AchievesGoal(description = "产出回答")
    public TenantAnswer ask(TenantRequest request, UserInput userInput, Ai ai) {
        String content = ai.withLlmByRole(request.tier())
                .withId("tenant-" + request.tenantId())
                .generateText("请用简洁的中文回答：\n" + userInput.getContent());
        return new TenantAnswer(request.tenantId(), request.tier(), content);
    }
}
