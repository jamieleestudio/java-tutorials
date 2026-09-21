package com.third.li;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义 {@code ChatMemoryStore}：把对话历史持久化到本地 JSON 文件，
 * 进程重启后同一 memoryId 的历史仍在 —— 对照 SAA 的 ChatMemoryStore、
 * Mastra 的 LibSQLStore（同类抽象，不同框架）。
 */
@Configuration
public class PersistentMemoryConfig {

    /** 极简 JSON 文件存储（教学用；生产换数据库实现）。 */
    static class FileChatMemoryStore implements dev.langchain4j.store.memory.chat.ChatMemoryStore {

        private final Path file = Path.of("chat-memory.json");
        private final Map<Object, List<ChatMessage>> memory = new HashMap<>();

        FileChatMemoryStore() {
            if (Files.exists(file)) {
                try {
                    for (String line : Files.readAllLines(file)) {
                        if (line.isBlank()) continue;
                        int sep = line.indexOf('|');
                        Object id = line.substring(0, sep);
                        String text = line.substring(sep + 1);
                        memory.computeIfAbsent(id, k -> new ArrayList<>())
                                .add(dev.langchain4j.data.message.UserMessage.from(text));
                    }
                } catch (IOException ignored) {
                }
            }
        }

        @Override
        public List<ChatMessage> getMessages(Object memoryId) {
            return new ArrayList<>(memory.getOrDefault(memoryId, List.of()));
        }

        @Override
        public void updateMessages(Object memoryId, List<ChatMessage> messages) {
            memory.put(memoryId, new ArrayList<>(messages));
            persist();
        }

        @Override
        public void deleteMessages(Object memoryId) {
            memory.remove(memoryId);
            persist();
        }

        private void persist() {
            try {
                List<String> lines = new ArrayList<>();
                memory.forEach((id, msgs) -> msgs.forEach(m -> lines.add(id + "|" + m.toString())));
                Files.write(file, lines);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public interface PersistentAssistant {

        String chat(String message);
    }

    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-chat")
                .build();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        ChatMemoryStore store = new FileChatMemoryStore();
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();
    }

    @Bean
    public PersistentAssistant persistentAssistant(ChatModel chatModel, ChatMemoryProvider provider) {
        return AiServices.builder(PersistentAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(provider)
                .build();
    }
}
