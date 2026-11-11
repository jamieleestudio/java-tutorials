package com.third.li;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.observability.api.event.AiServiceInvocationSummaryEvent;
import dev.langchain4j.observability.api.listener.AiServiceInvocationSummaryListener;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Agentic 可观测：{@code AgentMonitor} 记录整棵调用树（含耗时/token），
 * {@code HtmlReportGenerator} 可导出 HTML 执行报告 —— 与 SAA 的 AgentMonitor
 * 同名工具，Java 两大框架的可观测设计趋同。
 *
 * <p>1.23 新增：AI Service 侧的 {@link AiServiceInvocationSummaryEvent} ——
 * 一次 AI Service 调用的**全部事件汇总**（请求/工具调用/token/完成或失败），
 * 通过 {@code AiServices.registerListeners} 注册
 * {@link AiServiceInvocationSummaryListener} 即可拿到。
 */
@Configuration
public class AgenticObservabilityConfig {

    public interface StoryWriter {

        @UserMessage("围绕 {{topic}} 写一个不超过 3 句的故事草稿，只输出故事。")
        @Agent("Generates a story")
        String generateStory(@V("topic") String topic);
    }

    public interface StoryPolisher {

        @UserMessage("润色下面的故事，只输出润色结果：\n{{story}}")
        @Agent("Polishes a story")
        String polish(@V("story") String story);
    }

    /** 普通AI Service：演示 1.23 的调用汇总事件。 */
    public interface Assistant {

        @UserMessage("用一句话回答：{{question}}")
        String answer(@V("question") String question);
    }

    /** 捕获最近一次 {@link AiServiceInvocationSummaryEvent}。 */
    public static class CapturingSummaryListener implements AiServiceInvocationSummaryListener {

        private final AtomicReference<AiServiceInvocationSummaryEvent> last = new AtomicReference<>();

        @Override
        public void onEvent(AiServiceInvocationSummaryEvent event) {
            last.set(event);
        }

        public AiServiceInvocationSummaryEvent last() {
            return last.get();
        }
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
    public AgentMonitor agentMonitor() {
        return new AgentMonitor();
    }

    @Bean
    public UntypedAgent monitoredWorkflow(ChatModel chatModel, AgentMonitor monitor) {
        StoryWriter writer = AgenticServices.agentBuilder(StoryWriter.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        StoryPolisher polisher = AgenticServices.agentBuilder(StoryPolisher.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        return AgenticServices.sequenceBuilder()
                .subAgents(writer, polisher)
                .listener(monitor)
                .outputKey("story")
                .build();
    }

    @Bean
    public CapturingSummaryListener capturingSummaryListener() {
        return new CapturingSummaryListener();
    }

    @Bean
    public Assistant summarizableAssistant(ChatModel chatModel, CapturingSummaryListener listener) {
        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .registerListeners(listener)
                .build();
    }
}
