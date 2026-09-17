package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.skill.curator.SkillCuratorConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 技能策展（SkillCurator + SkillPromoter 自动升）。
 *
 * <p>AgentScope 的技能策展系统会<b>自动</b>评估技能使用情况：
 * <ul>
 *   <li>{@code SkillCurator} — 定期扫描技能使用统计，自动晋升/归档</li>
 *   <li>{@code SkillPromoter} — 把 draft 技能晋升为正式技能</li>
 *   <li>{@code SkillUsageStore} — 记录每个技能的 view/use 次数</li>
 * </ul>
 *
 * <p>通过 {@link HarnessAgent.Builder#enableSkillCurator(SkillCuratorConfig)} 启用。
 * 配置项包括：
 * <ul>
 *   <li>{@code intervalHours} — 策展间隔（小时）</li>
 *   <li>{@code minIdleHours} — 最小空闲时间（晋升前需要稳定使用多久）</li>
 *   <li>{@code staleAfterDays} — 多少天不用则标记过期</li>
 *   <li>{@code archiveAfterDays} — 多少天不用则归档</li>
 * </ul>
 *
 * <p>手动触发：{@code agent.runCuratorOnce()} 执行一轮策展，
 * {@code agent.promoteSkill(name, reason)} 手动晋升。
 */
@Component
public class CuratorAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public CuratorAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String chat(String message) {
        return agent().call(new UserMessage(message), runtimeContext()).block().getTextContent();
    }

    /** 手动触发一轮策展。 */
    public String runCurator() {
        return agent().runCuratorOnce()
                .map(report -> "策展完成：" + report.toString())
                .onErrorReturn("策展执行出错")
                .block();
    }

    /** 手动晋升指定技能。 */
    public String promoteSkill(String skillName, String reason) {
        return agent().promoteSkill(skillName, reason != null ? reason : "手动晋升")
                .map(result -> "晋升结果：" + result.toString())
                .onErrorReturn("晋升失败：技能不存在或已是正式技能")
                .block();
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    SkillCuratorConfig curatorConfig = SkillCuratorConfig.builder()
                            .enabled(true)
                            .intervalHours(1)
                            .staleAfterDays(30)
                            .archiveAfterDays(90)
                            .build();
                    local = HarnessAgent.builder()
                            .name("skill-curator")
                            .sysPrompt("你是一个乐于助人的助手。你拥有技能策展能力。")
                            .model(model)
                            .enableSkillCurator(curatorConfig)
                            .enableSkillManageTool(true)
                            .workspace(Paths.get(".agentscope/workspace-skill-curator"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("curator-demo").userId("alice").build();
    }
}