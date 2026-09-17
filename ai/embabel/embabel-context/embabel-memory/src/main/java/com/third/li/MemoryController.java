package com.third.li;

import com.embabel.agent.api.common.AiBuilder;
import com.embabel.agent.api.identity.SimpleUser;
import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.Identities;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.EmbeddingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 长期记忆接口。
 *
 * <p>用法演示"两个会话"：
 * <pre>
 *   会话 1：POST /memory/remember?userId=alice&text=我偏好用 Kotlin 写 Agent
 *   会话 2：GET  /memory/answer?userId=alice&question=给我写个最小 Agent 示例
 * </pre>
 * 第二个请求是**全新会话**，没有任何会话历史，但答案会体现"偏好 Kotlin"。
 */
@RestController
public class MemoryController {

    private final MemoryStore store;
    private final AiBuilder aiBuilder;
    private final AgentPlatform agentPlatform;

    public MemoryController(MemoryStore store, AiBuilder aiBuilder, AgentPlatform agentPlatform) {
        this.store = store;
        this.aiBuilder = aiBuilder;
        this.agentPlatform = agentPlatform;
    }

    /** 记住一件事（偏好或事实）。 */
    @PostMapping("/memory/remember")
    public MemoryItem remember(
            @RequestParam(value = "userId", defaultValue = "alice") String userId,
            @RequestParam(value = "text") String text,
            @RequestParam(value = "kind", defaultValue = "preference") String kind) {
        EmbeddingService embeddings = aiBuilder.ai().withDefaultEmbeddingService();
        return store.remember(userId, kind, text, embeddings.embed(text));
    }

    /** 原始召回（看得到相似度）。 */
    @GetMapping("/memory/recall")
    public List<MemoryItem> recall(
            @RequestParam(value = "userId", defaultValue = "alice") String userId,
            @RequestParam("query") String query,
            @RequestParam(value = "k", defaultValue = "3") int k) {
        EmbeddingService embeddings = aiBuilder.ai().withDefaultEmbeddingService();
        return store.recall(userId, embeddings.embed(query), k);
    }

    /** **新会话**里带记忆作答（身份通过 Identities 传入）。 */
    @GetMapping("/memory/answer")
    public MemoryAnswer answer(
            @RequestParam(value = "userId", defaultValue = "alice") String userId,
            @RequestParam("question") String question) {
        ProcessOptions options = ProcessOptions.DEFAULT.withIdentities(
                new Identities(new SimpleUser(userId, userId, userId, userId + "@example.com"), null));
        return AgentInvocation.builder(agentPlatform)
                .options(options)
                .build(MemoryAnswer.class)
                .invoke(new UserInput(question));
    }

    /** 忘掉某个用户的全部记忆（GDPR 意义上的"被遗忘权"最小实现）。 */
    @PostMapping("/memory/forget")
    public Map<String, Object> forget(@RequestParam(value = "userId", defaultValue = "alice") String userId) {
        int deleted = store.forget(userId);
        return Map.of("userId", userId, "deleted", deleted);
    }

    /** 记忆条数。 */
    @GetMapping("/memory/stats")
    public Map<String, Object> stats(@RequestParam(value = "userId", defaultValue = "alice") String userId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", userId);
        out.put("memories", store.count(userId));
        return out;
    }
}
