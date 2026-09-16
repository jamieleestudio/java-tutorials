package com.third.li;

import com.embabel.agent.api.common.AiBuilder;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.chat.AssistantMessage;
import com.embabel.chat.UserMessage;
import com.embabel.chat.support.InMemoryConversation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 多轮对话接口（阻塞式）。
 *
 * <p>用 Embabel 的 {@link InMemoryConversation} 保存每个会话的历史，
 * 每轮把完整历史（{@code List<Message>}）交给 {@link PromptRunner#respond}，
 * 并通过 {@code withSystemPrompt} 固定助手人格，从而获得跨轮次的记忆。
 *
 * <p>示例（同一个 sessionId 连续调用即可验证记忆）：
 * <pre>
 *   POST /chat/demo-1?message=我叫小明，养了一只猫
 *   POST /chat/demo-1?message=我叫什么？养了什么宠物？
 * </pre>
 *
 * <p>需要"边生成边显示"用 {@link StreamingController}（同一个会话存储，可混用）。
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    /** 助手人格：两个控制器共用（见 {@link StreamingController}）。 */
    static final String SYSTEM_PROMPT =
            "你是 Embabel 教程中的多轮对话助手。请用简洁的中文回答，并记住之前对话中的用户信息。";

    private final AiBuilder aiBuilder;
    private final ConversationStore store;

    public ChatController(AiBuilder aiBuilder, ConversationStore store) {
        this.aiBuilder = aiBuilder;
        this.store = store;
    }

    @PostMapping("/{sessionId}")
    public ChatResult chat(
            @PathVariable String sessionId,
            @RequestParam(value = "message") String message) {
        InMemoryConversation conversation = store.get(sessionId);
        conversation.addMessage(new UserMessage(message));

        PromptRunner runner = aiBuilder.ai().withDefaultLlm().withSystemPrompt(SYSTEM_PROMPT);
        AssistantMessage reply = runner.respond(conversation.getMessages());
        conversation.addMessage(reply);

        return new ChatResult(sessionId, reply.getContent(), conversation.getMessages().size());
    }

    @PostMapping("/{sessionId}/reset")
    public Map<String, Object> reset(@PathVariable String sessionId) {
        store.reset(sessionId);
        return Map.of("sessionId", sessionId, "reset", true);
    }
}
