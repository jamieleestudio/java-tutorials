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
 * Multi-Goal 模式（多目标选择）。
 *
 * <p>同一个 Agent 面对不同类型的请求，返回<b>不同类型</b>的结构化结果。
 * 模型根据用户意图自动选择"目标类型"——类似 Embabel 的
 * {@code embabel-multi-goal}（多目标自动选择 + 排序器），
 * 但 AgentScope 用 {@code call(msgs, JsonNode)} 的 JSON Schema 约束输出。
 *
 * <p>本模块演示 2 类目标：
 * <ul>
 *   <li>询问"是什么" → 返回 Definition（定义）</li>
 *   <li>询问"如何做" → 返回 Recipe（步骤清单）</li>
 * </ul>
 */
@Component
public class MultiGoalAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile HarnessAgent agent;

    public MultiGoalAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 目标 1：定义型输出 { "name": ..., "definition": ... }
     */
    public String askDefinition(String message) {
        JsonNode schema = definitionSchema();
        Msg result = agent().call(
                java.util.List.of(new UserMessage("请用定义形式回答：" + message)), schema, runtimeContext()).block();
        return "【定义型目标】\n" + pretty(result.getTextContent());
    }

    private JsonNode definitionSchema() {
        com.fasterxml.jackson.databind.node.ObjectNode root = objectMapper.createObjectNode();
        com.fasterxml.jackson.databind.node.ObjectNode props = root.putObject("properties");
        props.putObject("name").put("type", "string");
        props.putObject("definition").put("type", "string");
        return root;
    }

    /**
     * 目标 2：步骤型输出 { "title": ..., "steps": [...] }
     */
    public String askRecipe(String message) {
        JsonNode schema = recipeSchema();
        Msg result = agent().call(
                java.util.List.of(new UserMessage("请用步骤清单形式回答：" + message)), schema, runtimeContext()).block();
        return "【步骤型目标】\n" + pretty(result.getTextContent());
    }

    private JsonNode recipeSchema() {
        com.fasterxml.jackson.databind.node.ObjectNode root = objectMapper.createObjectNode();
        com.fasterxml.jackson.databind.node.ObjectNode props = root.putObject("properties");
        props.putObject("title").put("type", "string");
        props.putArray("steps").addObject().put("type", "string");
        return root;
    }

    /**
     * 目标 3：对比型输出 { "topic": ..., "pros": [...], "cons": [...] }
     */
    public String askComparison(String message) {
        JsonNode schema = comparisonSchema();
        Msg result = agent().call(
                java.util.List.of(new UserMessage("请用优缺点对比形式回答：" + message)), schema, runtimeContext()).block();
        return "【对比型目标】\n" + pretty(result.getTextContent());
    }

    private JsonNode comparisonSchema() {
        com.fasterxml.jackson.databind.node.ObjectNode root = objectMapper.createObjectNode();
        com.fasterxml.jackson.databind.node.ObjectNode props = root.putObject("properties");
        props.putObject("topic").put("type", "string");
        props.putArray("pros").addObject().put("type", "string");
        props.putArray("cons").addObject().put("type", "string");
        return root;
    }

    private String pretty(String json) {
        if (json == null) return "(空)";
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                    objectMapper.readTree(json));
        } catch (Exception e) {
            return json;
        }
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("multi-goal")
                            .sysPrompt("你是一个多面手助手。根据要求的输出格式返回对应的结构化 JSON，"
                                    + "不要输出多余内容。")
                            .model(model)
                            .workspace(Paths.get(".agentscope/agentscope-multi-goal"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("multi-goal").userId("alice").build();
    }
}