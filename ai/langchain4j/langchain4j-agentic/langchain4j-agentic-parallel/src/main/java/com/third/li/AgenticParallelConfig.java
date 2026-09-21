package com.third.li;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 并行工作流：{@code parallelBuilder} 让两个独立专家并发执行，
 * {@code output(...)} 把 AgenticScope 里的两份输出聚合成最终结果。
 */
@Configuration
public class AgenticParallelConfig {

    public interface FoodExpert {

        @UserMessage("""
                你是晚餐规划师。根据氛围 {{mood}} 推荐 3 道菜，只输出菜名列表。
                """)
        @Agent("Recommends meals for a mood")
        List<String> findMeal(@V("mood") String mood);
    }

    public interface MovieExpert {

        @UserMessage("""
                你是电影顾问。根据氛围 {{mood}} 推荐 3 部电影，只输出片名列表。
                """)
        @Agent("Recommends movies for a mood")
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
                .outputKey("plans")
                .output(agenticScope -> {
                    List<?> meals = agenticScope.readState("meals", List.of());
                    List<?> movies = agenticScope.readState("movies", List.of());
                    return java.util.stream.IntStream.range(0,
                                    Math.max(meals.size(), movies.size()))
                            .mapToObj(i -> (i < movies.size() ? movies.get(i) : "?")
                                    + " + " + (i < meals.size() ? meals.get(i) : "?"))
                            .toList();
                })
                .build();
    }
}
