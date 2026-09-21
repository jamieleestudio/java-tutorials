package com.third.li;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 并行模式：两位专家并发 + output 聚合（对照 SAA parallel edges / Mastra parallel）。 */
@Configuration
public class PatternParallelConfig {

    public interface FoodExpert {

        @UserMessage("根据氛围 {{mood}} 推荐 3 道菜，只输出菜名列表。")
        @Agent("Recommends meals")
        List<String> findMeal(@V("mood") String mood);
    }

    public interface MovieExpert {

        @UserMessage("根据氛围 {{mood}} 推荐 3 部电影，只输出片名列表。")
        @Agent("Recommends movies")
        List<String> findMovie(@V("mood") String mood);
    }

    public interface EveningPlannerAgent {

        @Agent
        List<String> plan(@V("mood") String mood);
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
    public EveningPlannerAgent eveningPlannerAgent(ChatModel chatModel) {
        FoodExpert foodExpert = AgenticServices.agentBuilder(FoodExpert.class)
                .chatModel(chatModel)
                .outputKey("meals")
                .build();
        MovieExpert movieExpert = AgenticServices.agentBuilder(MovieExpert.class)
                .chatModel(chatModel)
                .outputKey("movies")
                .build();

        return AgenticServices.parallelBuilder(EveningPlannerAgent.class)
                .subAgents(foodExpert, movieExpert)
                .executor(java.util.concurrent.Executors.newFixedThreadPool(2))
                .outputKey("plans")
                .output(agenticScope -> {
                    List<?> meals = agenticScope.readState("meals", List.of());
                    List<?> movies = agenticScope.readState("movies", List.of());
                    return java.util.stream.IntStream.range(0, Math.min(meals.size(), movies.size()))
                            .mapToObj(i -> movies.get(i) + " + " + meals.get(i))
                            .toList();
                })
                .build();
    }
}
