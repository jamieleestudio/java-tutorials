package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.Context;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.ContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 持久化接口。
 *
 * <p>典型验证方式：
 * <ol>
 *   <li>{@code GET /persistence/save?userId=u1&name=张三&plan=pro} 保存上下文</li>
 *   <li>重启应用（上下文已在 Postgres 里）</li>
 *   <li>{@code GET /persistence/load?userId=u1} 仍能读回，且类型是 {@link UserProfile}</li>
 *   <li>{@code GET /persistence/agent?userId=u1&message=...} Agent 使用该画像回答</li>
 * </ol>
 */
@RestController
public class PersistenceController {

    private final ContextRepository contextRepository;
    private final AgentPlatform agentPlatform;

    public PersistenceController(ContextRepository contextRepository, AgentPlatform agentPlatform) {
        this.contextRepository = contextRepository;
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/persistence/save")
    public Map<String, Object> save(
            @RequestParam(value = "userId", defaultValue = "u1") String userId,
            @RequestParam(value = "name", defaultValue = "张三") String name,
            @RequestParam(value = "plan", defaultValue = "pro") String plan) {
        Context context = contextRepository.createWithId(userId);
        context.bind("profile", new UserProfile(userId, name, plan));
        contextRepository.save(context);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contextId", userId);
        result.put("saved", true);
        result.put("objects", context.getObjects());
        return result;
    }

    @GetMapping("/persistence/load")
    public Map<String, Object> load(@RequestParam(value = "userId", defaultValue = "u1") String userId) {
        Context context = contextRepository.findById(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("contextId", userId);
        if (context == null) {
            result.put("found", false);
            return result;
        }
        result.put("found", true);
        result.put("objects", context.getObjects());
        result.put("types", context.getObjects().stream().map(o -> o.getClass().getName()).toList());
        return result;
    }

    @GetMapping("/persistence/agent")
    public ContextualAnswer agent(
            @RequestParam(value = "userId", defaultValue = "u1") String userId,
            @RequestParam(value = "message", defaultValue = "推荐一个适合我的套餐") String message) {
        return AgentInvocation.create(agentPlatform, ContextualAnswer.class)
                .invoke(new UserId(userId), new UserInput(message));
    }

    @GetMapping("/persistence/delete")
    public Map<String, Object> delete(@RequestParam(value = "userId", defaultValue = "u1") String userId) {
        Context context = contextRepository.findById(userId);
        if (context != null) {
            contextRepository.delete(context);
        }
        return Map.of("contextId", userId, "deleted", context != null);
    }
}
