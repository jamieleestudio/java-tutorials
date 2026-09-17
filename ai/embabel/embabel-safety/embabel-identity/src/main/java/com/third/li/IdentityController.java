package com.third.li;

import com.embabel.agent.api.identity.SimpleUser;
import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.Identities;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 身份与上下文接口。
 *
 * <p>两个端点演示两件事：身份（谁在问）与请求级元数据（这次请求属于哪个租户）。
 */
@RestController
public class IdentityController {

    private final AgentPlatform agentPlatform;
    private final InMemoryUserService userService;

    public IdentityController(AgentPlatform agentPlatform, InMemoryUserService userService) {
        this.agentPlatform = agentPlatform;
        this.userService = userService;
    }

    /** 只看身份：谁在问、以谁的身份执行。 */
    @GetMapping("/identity/whoami")
    public IdentityReport whoami(
            @RequestParam(value = "username", defaultValue = "alice") String username) {
        return run(username, null);
    }

    /**
     * 身份 + 租户元数据：同一个用户，不同租户上下文，工具返回不同数据。
     *
     * <p>对比 {@code /identity/whoami?username=bob}：Bob 属于 globex，
     * 但这里由**请求方**声明租户——真实系统里租户应来自认证后的会话，而不是请求参数。
     */
    @GetMapping("/identity/orders")
    public IdentityReport orders(
            @RequestParam(value = "username", defaultValue = "alice") String username,
            @RequestParam(value = "tenant", defaultValue = "acme") String tenant) {
        return run(username, tenant);
    }

    private IdentityReport run(String username, String tenant) {
        SimpleUser user = userService.findByUsername(username);

        // 身份：为谁执行（forUser）+ 以谁的身份执行（runAs，服务账号，用于越权代表）
        Identities identities = new Identities(user, userService.serviceAccount());

        ProcessOptions options = ProcessOptions.DEFAULT.withIdentities(identities);
        if (tenant != null) {
            // 请求级元数据：随请求生命周期存在，会被传给每一个工具（含 MCP）
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("tenantId", tenant);
            context.put("userId", user.getId());
            context.put("requestId", UUID.randomUUID().toString().substring(0, 8));
            options = options.withToolCallContext(context);
        }

        return AgentInvocation.builder(agentPlatform)
                .options(options)
                .build(IdentityReport.class)
                .invoke(new UserInput("查看我的订单"));
    }
}
