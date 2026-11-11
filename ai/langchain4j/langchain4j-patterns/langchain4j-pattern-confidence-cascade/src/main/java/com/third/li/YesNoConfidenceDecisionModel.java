package com.third.li;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.request.YesNoQuestion;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.decision.response.YesNoAnswer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 仅支持 是/否 问题的决策模型（本次级联只需要"置信度足够吗"一个问题）。
 * 与 decision-models 模块的 {@code ChatModelDecisionModel} 同思路：
 * 把问题打包成一次结构化 JSON 调用，映射回 {@link YesNoAnswer}。
 */
public class YesNoConfidenceDecisionModel implements DecisionModel {

    private final ChatModel chatModel;
    private final ObjectMapper mapper = new ObjectMapper();

    public YesNoConfidenceDecisionModel(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public DecisionResponse doDecide(DecisionRequest request) {
        StringBuilder prompt = new StringBuilder(
                "你是决策模型。基于输入回答全部问题，只输出严格 JSON。\n输入：\n");
        request.questions().forEach((name, q) -> {
            if (q instanceof YesNoQuestion ynq) {
                prompt.append("- 问题名 ").append(name).append("（是/否）：").append(ynq.text())
                        .append("（是= ").append(ynq.yesWhen()).append("；否= ").append(ynq.noWhen()).append("）\n");
            }
        });
        prompt.append("输出格式：{\"answers\":{\"<问题名>\":{\"probabilities\":{\"yes\":0.0,\"no\":0.0}}}}");

        String raw = chatModel.chat(prompt.toString());
        JsonNode answers = mapper.readTree(stripFences(raw)).path("answers");

        DecisionResponse.Builder builder = DecisionResponse.builder().modelName("chat-model-backed");
        request.questions().forEach((name, q) -> {
            if (q instanceof YesNoQuestion) {
                double yesProbability = answers.path(name).path("probabilities").path("yes").asDouble(0.5);
                builder.answer(name, YesNoAnswer.builder().probability(yesProbability).build());
            }
        });
        return builder.build();
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
