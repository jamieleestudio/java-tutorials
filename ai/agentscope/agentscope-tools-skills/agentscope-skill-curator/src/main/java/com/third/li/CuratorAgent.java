package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.remote.store.InMemoryStore;
import io.agentscope.harness.agent.skill.curator.EnvironmentFilter;
import io.agentscope.harness.agent.skill.curator.LocalApprovalGate;
import io.agentscope.harness.agent.skill.curator.SkillCuratorConfig;
import io.agentscope.harness.agent.skill.curator.SkillPromotionGate;
import io.agentscope.harness.agent.skill.curator.SkillUsageStore;
import io.agentscope.harness.agent.skill.curator.SkillVisibilityFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * 技能策展 Agent：开启 {@code SkillCurator}，让 Agent 自动管理技能生命周期——
 * 根据使用频率自动晋升（promote）/归档（archive）技能。
 *
 * <p>关键 Builder 方法：
 * <ul>
 *   <li>{@code enableSkillCurator(SkillCuratorConfig)} —— 开启策展，配置运行间隔、过期天数</li>
 *   <li>{@code enableSkillPromotionGate(SkillPromotionGate, SkillVisibilityFilter)} ——
 *       设置晋升审批门 + 可见性过滤器</li>
 * </ul>
 *
 * <p>本例用 {@link LocalApprovalGate}（本地审批，默认超时 30s）作为晋升门，
 * {@link EnvironmentFilter} 按环境过滤可见技能。技能用量存储用内存 {@link InMemoryStore}
 * （生产环境应换成持久化的 store）。
 */
@Component
public class CuratorAgent {

    private static final String WORKSPACE_DIR = ".agentscope/workspace-skill-curator";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private final SkillUsageStore usageStore = SkillUsageStore.baseStore(new InMemoryStore());
    private volatile HarnessAgent agent;

    public CuratorAgent(
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

    /** 手动触发一次策展运行。 */
    public String runCuratorOnce() {
        var report = agent().runCuratorOnce().block();
        return "策展运行完成：" + (report != null ? report.toString() : "无报告");
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    SkillCuratorConfig curatorConfig = SkillCuratorConfig.builder()
                            .enabled(true)
                            .intervalHours(1)
                            .staleAfterDays(7)
                            .archiveAfterDays(30)
                            .build();
                    SkillPromotionGate gate = new LocalApprovalGate(java.time.Duration.ofSeconds(30));
                    SkillVisibilityFilter filter = new EnvironmentFilter("demo", usageStore);

                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    local = HarnessAgent.builder()
                            .name("skill-curator")
                            .sysPrompt("你是一个智能助手，可使用技能。系统会自动策展技能生命周期。")
                            .model(model)
                            .enableSkillCurator(curatorConfig)
                            .enableSkillPromotionGate(gate, filter)
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
                .sessionId("curator-demo").userId("alice").build();
    }
}