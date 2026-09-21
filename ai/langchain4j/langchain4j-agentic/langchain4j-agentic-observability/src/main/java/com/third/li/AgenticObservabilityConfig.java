package com.third.li;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agentic 可观测：{@code AgentMonitor} 记录整棵调用树（含耗时/token），
 * {@code HtmlReportGenerator} 可导出 HTML 执行报告 —— 与 SAA 的 AgentMonitor
 * 同名工具，Java 两大框架的可观测设计趋同。
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
}
