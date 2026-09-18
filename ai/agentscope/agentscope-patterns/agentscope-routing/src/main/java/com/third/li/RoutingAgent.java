package com.third.li;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * Routing 模式（路由分类）。
 *
 * <p>先用一个"分类器 Agent"把用户输入分成预定义类别，
 * 然后 <b>switch</b> 到不同类别的专用 Agent 处理。
 *
 * <p>与 Embabel 的 {@code @Condition} 不同——Embabel 在流程里静态选择 path；
 * 本模式用<b>结构化输出</b>（分类标签）在运行时动态路由，
 * 而且每个类别有独立的 Agent（可以有不同的系统提示/工具）。
 *
 * <p>本模块演示 3 类路由：
 * <ul>
 *   <li>{@code 技术} → techAgent（强调技术细节）</li>
 *   <li>{@code 生活} → lifeAgent（生活化口吻）</li>
 *   <li>{@code 其它} → defaultAgent（通用助手）</li>
 * </ul>
 */
@Component
public class RoutingAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile HarnessAgent classifier;
    private volatile HarnessAgent techAgent;
    private volatile HarnessAgent lifeAgent;
    private volatile HarnessAgent defaultAgent;

    public RoutingAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 路由：分类 → 分发到专用 Agent。
     */
    public String route(String message) {
        RuntimeContext ctx = runtimeContext();

        // 第 1 步：结构化输出分类结果 { "category": "技术" }
        Msg classifyResult = classifier().call(
                java.util.List.of(new UserMessage(message)),
                objectMapper.createObjectNode()
                        .set("properties", objectMapper.createObjectNode()
                                .put("category", objectMapper.createObjectNode()
                                        .put("type", "string")
                                        .put("description", "类别：技术/生活/其它"))),
                ctx).block();

        String category = extractCategory(classifyResult);
        Msg answer;
        switch (category) {
            case "技术" -> {
                answer = techAgent().call(new UserMessage(message), ctx).block();
                return "【路由到：技术 Agent】\n" + answer.getTextContent();
            }
            case "生活" -> {
                answer = lifeAgent().call(new UserMessage(message), ctx).block();
                return "【路由到：生活 Agent】\n" + answer.getTextContent();
            }
            default -> {
                answer = defaultAgent().call(new UserMessage(message), ctx).block();
                return "【路由到：默认 Agent】\n" + answer.getTextContent();
            }
        }
    }

    private String extractCategory(Msg classifyResult) {
        String text = classifyResult.getTextContent();
        if (text != null) {
            try {
                JsonNode node = objectMapper.readTree(text);
                if (node.has("category")) {
                    return node.get("category").asText();
                }
            } catch (Exception ignored) {
                // 结构化输出解析失败，回退到文本匹配
            }
            if (text.contains("技术")) return "技术";
            if (text.contains("生活")) return "生活";
        }
        return "其它";
    }

    private HarnessAgent classifier() {
        HarnessAgent local = classifier;
        if (local == null) {
            synchronized (this) {
                local = classifier;
                if (local == null) {
                    local = buildAgent("classifier", "你是一个分类器。把用户输入分类为：技术 / 生活 / 其它。"
                            + "只输出 JSON：{\"category\": \"技术\"}，不要多余内容。");
                    classifier = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent techAgent() {
        HarnessAgent local = techAgent;
        if (local == null) {
            synchronized (this) {
                local = techAgent;
                if (local == null) {
                    local = buildAgent("tech-agent", "你是技术专家。用严谨的技术视角回答，" +
                            "给出原理、代码示例和最佳实践。");
                    techAgent = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent lifeAgent() {
        HarnessAgent local = lifeAgent;
        if (local == null) {
            synchronized (this) {
                local = lifeAgent;
                if (local == null) {
                    local = buildAgent("life-agent", "你是生活助手。用轻松、实用的口吻回答生活类问题，" +
                            "给出可操作的建议。");
                    lifeAgent = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent defaultAgent() {
        HarnessAgent local = defaultAgent;
        if (local == null) {
            synchronized (this) {
                local = defaultAgent;
                if (local == null) {
                    local = buildAgent("default-agent", "你是一个通用助手，回答用户的问题。");
                    defaultAgent = local;
                }
            }
        }
        return local;
    }

    private HarnessAgent buildAgent(String name, String sysPrompt) {
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
        return HarnessAgent.builder()
                .name(name)
                .sysPrompt(sysPrompt)
                .model(model)
                .workspace(Paths.get(".agentscope/agentscope-routing"))
                .build();
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("routing").userId("alice").build();
    }
}