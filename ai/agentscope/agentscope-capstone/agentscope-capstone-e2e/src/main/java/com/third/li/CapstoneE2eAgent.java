package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.rag.GenericRAGHook;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.model.DocumentMetadata;
import io.agentscope.core.rag.model.RetrieveConfig;
import io.agentscope.core.state.JsonFileAgentStateStore;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.MemoryConfig;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 端到端综合示例 —— 把 RAG 检索 + 记忆管理 + 工具调用 + 状态持久化 + 上下文压缩
 * 合成一个完整的生产级 Agent。
 *
 * <p>综合能力：
 * <ul>
 *   <li><b>RAG</b>：{@link GenericRAGHook} + 内存 {@link Knowledge}，回答时检索知识库</li>
 *   <li><b>记忆</b>：{@link MemoryConfig} 管理会话记忆，支持跨轮上下文</li>
 *   <li><b>工具</b>：注册业务工具（时间查询 + 翻译），模型自主调用</li>
 *   <li><b>持久化</b>：{@link JsonFileAgentStateStore} 把状态存磁盘，重启可恢复</li>
 *   <li><b>压缩</b>：{@link CompactionConfig} 长对话自动压缩</li>
 * </ul>
 *
 * <p>这是整个教程的集大成模块：一个 Agent 同时具备知识检索、记忆、工具、
 * 持久化、压缩五大能力，演示 AgentScope 的完整工程化范式。
 */
@Component
public class CapstoneE2eAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-capstone-e2e";
    private static final String STATE_DIR = ".agentscope/state-capstone-e2e";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public CapstoneE2eAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    InMemoryKnowledge knowledge = new InMemoryKnowledge();
                    knowledge.addDocuments(List.of(
                            doc("e2e-1", "AgentScope 教程涵盖基础、中间件、工具、权限、工作区、记忆、RAG、服务八大类。"),
                            doc("e2e-2", "AgentScope 的 ReAct 循环让模型自主决策，Middleware 链做约束。"),
                            doc("e2e-3", "AgentScope 支持 InMemoryMemory 短期记忆和 LongTermMemory 长期记忆。")
                    )).block();
                    GenericRAGHook ragHook = new GenericRAGHook(knowledge,
                            RetrieveConfig.builder().limit(3).scoreThreshold(0.0).build());

                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(new MiscTools());

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("capstone-e2e")
                            .sysPrompt("你是一个综合助手，具备知识检索、记忆、工具调用能力。"
                                    + "回答时参考检索到的知识库内容，可调用工具获取时间或翻译。")
                            .model(model)
                            .toolkit(toolkit)
                            .hook(ragHook)
                            .memory(MemoryConfig.builder().model(model).sessionRetentionDays(30).build())
                            .compaction(CompactionConfig.builder()
                                    .triggerTokens(4000)
                                    .keepMessages(8)
                                    .summaryPrompt("请用中文总结对话关键信息。")
                                    .build())
                            .stateStore(new JsonFileAgentStateStore(Paths.get(STATE_DIR).toAbsolutePath()))
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("capstone-e2e-demo").userId("alice").build();
    }

    private static Document doc(String id, String text) {
        return new Document(new DocumentMetadata(TextBlock.builder().text(text).build(), id, "chunk-0"));
    }

    public static class MiscTools {
        @Tool(name = "getCurrentTime", description = "获取当前时间")
        public String getCurrentTime() {
            return java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        @Tool(name = "translate", description = "简单中英互译")
        public String translate(@ToolParam(name = "text", description = "待翻译文本") String text,
                                @ToolParam(name = "targetLang", description = "目标语言：en 或 zh") String targetLang) {
            if ("en".equalsIgnoreCase(targetLang)) {
                return "（翻译为英文）" + text;
            }
            return "（翻译为中文）" + text;
        }
    }

    static class InMemoryKnowledge implements Knowledge {
        private final List<Document> docs = new ArrayList<>();

        @Override
        public reactor.core.publisher.Mono<Void> addDocuments(List<Document> documents) {
            return reactor.core.publisher.Mono.fromRunnable(() -> docs.addAll(documents));
        }

        @Override
        public reactor.core.publisher.Mono<List<Document>> retrieve(String query, RetrieveConfig config) {
            return reactor.core.publisher.Mono.fromSupplier(() -> docs.stream()
                    .filter(d -> {
                        String text = d.getMetadata().getContentText();
                        return text.contains(query) || query.length() > 4
                                && text.contains(query.substring(0, 4));
                    })
                    .limit(config.getLimit())
                    .collect(Collectors.toList()));
        }
    }
}