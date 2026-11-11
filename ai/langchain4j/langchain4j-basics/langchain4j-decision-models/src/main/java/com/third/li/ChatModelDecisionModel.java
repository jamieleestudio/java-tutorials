package com.third.li;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.request.ChoiceQuestion;
import dev.langchain4j.model.decision.request.DecisionInput;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.request.MessagesInput;
import dev.langchain4j.model.decision.request.Question;
import dev.langchain4j.model.decision.request.ScaleQuestion;
import dev.langchain4j.model.decision.request.TextInput;
import dev.langchain4j.model.decision.request.YesNoQuestion;
import dev.langchain4j.model.decision.response.ChoiceAnswer;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.decision.response.ScaleAnswer;
import dev.langchain4j.model.decision.response.YesNoAnswer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用普通 ChatModel 实现 {@link DecisionModel} 契约。
 *
 * <p>LangChain4j 1.21 引入决策模型（@Experimental）：回答"问题 + 类型化答案 + 概率"
 * 而不是生成文本。官方集成有 TypeSafe / OpenAI Decisions API（{@code OpenAiDecisionModel}）；
 * DeepSeek 等兼容端点没有专用决策端点，因此这里演示**自己适配**：
 * 把 {@link DecisionRequest} 里的全部问题打包成一次结构化 JSON 调用，再映射回
 * {@link YesNoAnswer} / {@link ChoiceAnswer} / {@link ScaleAnswer}。
 *
 * <p>任何实现 {@link DecisionModel} 单方法契约的类，都能直接接入
 * {@code DecisionServices}、{@code DecisionModelChatModelRouter} 等整套生态。
 */
public class ChatModelDecisionModel implements DecisionModel {

    private final ChatModel chatModel;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatModelDecisionModel(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public DecisionResponse doDecide(DecisionRequest request) {
        String prompt = buildPrompt(request);
        String raw = chatModel.chat(prompt);
        JsonNode answers = mapper.readTree(stripFences(raw)).path("answers");

        DecisionResponse.Builder builder = DecisionResponse.builder().modelName("chat-model-backed");
        for (Map.Entry<String, Question> entry : request.questions().entrySet()) {
            String name = entry.getKey();
            Question question = entry.getValue();
            JsonNode a = answers.path(name);
            switch (question) {
                case YesNoQuestion ynq -> builder.answer(name, YesNoAnswer.builder()
                        .probability(a.path("probabilities").path("yes").asDouble(0.5))
                        .build());
                case ChoiceQuestion cq -> {
                    ChoiceAnswer.Builder b = ChoiceAnswer.builder()
                            .value(a.path("answer").asText())
                            .options(cq.options().keySet());
                    a.path("probabilities").properties()
                            .forEach(p -> b.probability(p.getKey(), p.getValue().asDouble()));
                    builder.answer(name, b.build());
                }
                case ScaleQuestion sq -> builder.answer(name, scaleAnswer(sq, a));
                default -> throw new IllegalArgumentException("不支持的问题类型：" + question.getClass());
            }
        }
        return builder.build();
    }

    private ScaleAnswer scaleAnswer(ScaleQuestion question, JsonNode answer) {
        List<String> levels = question.levels();
        List<Double> probs = new ArrayList<>();
        double[] sum = {0};
        for (String level : levels) {
            double p = answer.path("probabilities").path(level).asDouble(0.0);
            probs.add(p);
            sum[0] += p;
        }
        if (sum[0] > 0) {
            probs.replaceAll(p -> p / sum[0]);
        }
        double mean = 0;
        for (int i = 0; i < levels.size(); i++) {
            mean += (i + 1) * probs.get(i);
        }
        return ScaleAnswer.builder().mean(mean).probabilities(probs).build();
    }

    private String buildPrompt(DecisionRequest request) {
        StringBuilder prompt = new StringBuilder("你是决策模型。基于下面的输入回答全部问题，只输出严格 JSON。\n输入：\n")
                .append(extractInput(request.input())).append("\n问题：\n");
        request.questions().forEach((name, q) -> {
            prompt.append("- 问题名 ").append(name);
            switch (q) {
                case YesNoQuestion ynq -> prompt.append("（是/否）：").append(ynq.text())
                        .append("（是= ").append(ynq.yesWhen()).append("；否= ").append(ynq.noWhen()).append("）\n");
                case ChoiceQuestion cq -> prompt.append("（单选）：").append(cq.text())
                        .append(" 选项：").append(cq.options().keySet()).append("\n");
                case ScaleQuestion sq -> prompt.append("（评级）：").append(sq.text())
                        .append(" 级别：").append(sq.levels()).append("\n");
                default -> prompt.append("：").append(q).append("\n");
            }
        });
        prompt.append("""
                输出格式（不要输出其它内容）：
                {"answers":{"<问题名>":{"answer":"yes|no|<选项>|<级别>","probabilities":{"<取值>":0.0}}}}
                """);
        return prompt.toString();
    }

    private String extractInput(DecisionInput input) {
        if (input instanceof TextInput text) {
            return text.text();
        }
        if (input instanceof MessagesInput messages) {
            StringBuilder sb = new StringBuilder();
            for (ChatMessage message : messages.messages()) {
                if (message instanceof UserMessage user) {
                    sb.append(user.singleText()).append('\n');
                }
            }
            return sb.toString();
        }
        return String.valueOf(input);
    }

    private String stripFences(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(trimmed.indexOf('\n') + 1);
            int end = trimmed.lastIndexOf("```");
            if (end >= 0) {
                trimmed = trimmed.substring(0, end);
            }
        }
        return trimmed.trim();
    }
}
