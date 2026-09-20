package com.third.li;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Agent 记忆：saver（checkpoint）+ threadId（会话隔离）。
 *
 * <p>ReactAgent 的"多轮记忆"由两件事组成：
 * <ul>
 *   <li>{@code builder().saver(new MemorySaver())} — 会话状态（含完整消息历史）
 *       存入 checkpoint saver；生产可换 RedisSaver / MysqlSaver 等</li>
 *   <li>{@code call(input, RunnableConfig.builder().threadId(id).build())} —
 *       同一 threadId 的调用共享同一份历史，实现"记得之前说过什么"；
 *       不同 threadId 相互隔离</li>
 * </ul>
 *
 * <p>对照 {@code ai/spring-ai/spring-ai-advisors/spring-ai-chat-memory-advisor}（MessageWindowChatMemory）：
 * SAA 用 checkpoint 机制承载记忆，与 Graph 的中断/恢复能力共用同一套存储。
 */
@Service
public class AgentMemoryService {

    private final ReactAgent agent;
    private final ConcurrentMap<String, String> threads = new ConcurrentHashMap<>();

    public AgentMemoryService(ChatModel chatModel) throws GraphStateException {
        this.agent = ReactAgent.builder()
                .name("memory-assistant")
                .description("带会话记忆的助手")
                .systemPrompt("你是一个有短期记忆的助手。记住用户在本次会话告诉你的信息，回答简短。")
                .model(chatModel)
                .saver(new MemorySaver())
                .build();
    }

    /** 在指定会话（threadId）上对话；不传则新建会话。 */
    public String chat(String threadId, String message) throws GraphRunnerException {
        String tid = (threadId == null || threadId.isBlank())
                ? UUID.randomUUID().toString() : threadId;
        threads.putIfAbsent(tid, "active");
        RunnableConfig config = RunnableConfig.builder().threadId(tid).build();
        String answer = agent.call(message, config).getText();
        return "threadId: " + tid + "\n回答: " + answer;
    }
}
