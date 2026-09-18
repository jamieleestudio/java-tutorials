package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 技能系统 Agent：用 {@link SkillBox} 把技能注册到 {@link Toolkit}。
 *
 * <p>AgentScope 的"技能（Skill）"是比"工具（Tool）"更高层的封装：
 * 一个技能可以绑定一组工具 + 一段提示词 + 资源文件，按需激活/停用。
 * {@link SkillBox} 内部持有 {@link io.agentscope.core.skill.SkillRegistry}，
 * 通过 {@link SkillBox#registerSkill(AgentSkill)} 注册，
 * {@link SkillBox#setSkillActive(String, boolean)} 激活/用。
 *
 * <p>本例注册一个"天气查询"技能，绑定 {@link WeatherTools} 并附上技能提示词。
 * 运行时模型会看到技能清单，主动调用对应的工具。
 */
@Component
public class SkillAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-skills";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public SkillAgent(
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
                    Toolkit toolkit = new Toolkit();
                    SkillBox skillBox = new SkillBox(toolkit);
                    skillBox.registration()
                            .skill(AgentSkill.builder()
                                    .name("weather")
                                    .description("查询城市天气")
                                    .skillContent("当用户询问天气时，调用 getWeather 工具获取对应城市的天气信息并简洁回复。")
                                    .build())
                            .tool(new WeatherTools())
                            .apply();
                    skillBox.setSkillActive("weather", true);

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("skills-agent")
                            .sysPrompt("你是一个智能助手，可以按需激活技能完成任务。")
                            .model(model)
                            .toolkit(toolkit)
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
                .sessionId("skills-demo").userId("alice").build();
    }

    public static class WeatherTools {

        @Tool(name = "getWeather", description = "查询指定城市的天气")
        public String getWeather(@io.agentscope.core.tool.ToolParam(name = "city", description = "城市名") String city) {
            return city + "：晴，25°C，湿度 60%";
        }
    }
}