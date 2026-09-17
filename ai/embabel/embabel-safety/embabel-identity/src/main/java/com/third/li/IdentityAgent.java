package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.identity.User;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.api.tool.ToolCallContext;
import com.embabel.agent.core.Identities;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * **身份与请求级元数据**如何进入 Agent，并在工具层生效。
 *
 * <p>两条链路：
 * <ol>
 *   <li><b>身份</b>：调用方用 {@code ProcessOptions.withIdentities(Identities.forUser(user).withRunAs(serviceAccount))}
 *       传入"为谁执行 / 以谁的身份执行"；动作里通过
 *       {@link OperationContext#user()} 拿到当前用户（框架自动注入 {@code OperationContext} 参数）。</li>
 *   <li><b>请求级元数据</b>：用 {@code ProcessOptions.withToolCallContext(Map)} 传入租户/请求 ID/token，
 *       工具在 {@code call(input, ToolCallContext)} 里读取它做数据隔离。</li>
 * </ol>
 *
 * <p>注意动作签名里的 {@link OperationContext}：框架的
 * {@code OperationContextArgumentResolver} 支持注入它（判断条件是
 * {@code OperationContext.isAssignableFrom(参数类型)}），所以你可以直接拿到
 * 用户、进程上下文、黑板等——不用自己去找线程本地变量。
 */
@Agent(description = "身份与请求级元数据：用户身份进入 Agent，租户上下文透传到工具")
public class IdentityAgent {

    private static final Logger log = LoggerFactory.getLogger(IdentityAgent.class);

    private final OrderTools orderTools;

    public IdentityAgent(OrderTools orderTools) {
        this.orderTools = orderTools;
    }

    @Action(description = "产出身份与租户隔离报告")
    @AchievesGoal(description = "产出身份报告")
    public IdentityReport report(UserInput userInput, OperationContext context) {
        User forUser = context.user();
        ProcessOptions options = context.getProcessContext().getProcessOptions();
        Identities identities = options.getIdentities();
        ToolCallContext toolCallContext = options.getToolCallContext();

        log.info("forUser={}, runAs={}, toolCallContext={}",
                forUser == null ? null : forUser.getUsername(),
                identities.getRunAs() == null ? null : identities.getRunAs().getUsername(),
                toolCallContext.toMap());

        // 关键：动作把请求级元数据**显式**传给工具
        Tool scoped = orderTools.tenantScopedQuery();
        Tool leaky = orderTools.leakyQuery();

        return new IdentityReport(
                describe(forUser),
                describe(identities.getRunAs()),
                new LinkedHashMap<>(toolCallContext.toMap()),
                content(scoped.call("{}", toolCallContext)),
                content(leaky.call("{}")));
    }

    private Map<String, String> describe(User user) {
        if (user == null) {
            return null;
        }
        Map<String, String> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("displayName", user.getDisplayName());
        map.put("email", user.getEmail());
        return map;
    }

    private String content(Tool.Result result) {
        return result instanceof Tool.Result.Text text ? text.getContent() : result.toString();
    }
}
