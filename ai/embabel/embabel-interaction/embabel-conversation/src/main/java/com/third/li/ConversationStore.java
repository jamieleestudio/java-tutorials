package com.third.li;

import com.embabel.chat.support.InMemoryConversation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话存储：按 {@code sessionId} 保存多轮历史。
 *
 * <p>抽成独立组件后，**阻塞式对话**（{@link ChatController}）与
 * **流式对话**（{@link StreamingController}）共享同一份历史——
 * 所以可以先用流式接口说一句话，再用普通接口追问，模型仍然记得上文。
 *
 * <p>内存实现，进程重启即丢失；生产应换成持久化实现
 * （参考 {@code embabel-persistence}）。
 */
@Component
public class ConversationStore {

    private final Map<String, InMemoryConversation> sessions = new ConcurrentHashMap<>();

    /** 取出（或新建）某个会话。 */
    public InMemoryConversation get(String sessionId) {
        return sessions.computeIfAbsent(sessionId, id -> new InMemoryConversation(List.of(), id));
    }

    /** 清空某个会话的历史。 */
    public void reset(String sessionId) {
        sessions.remove(sessionId);
    }
}
