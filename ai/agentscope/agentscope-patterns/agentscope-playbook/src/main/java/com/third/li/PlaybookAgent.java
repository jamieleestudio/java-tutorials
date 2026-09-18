package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Map;

/**
 * Playbook 模式（解锁条件式技能集）。
 *
 * <p>一组"技能"（playbook），只有满足前置条件时才解锁对应的工具——
 * 与 Embabel 的 {@code embabel-playbook}（按前置工具是否执行收敛）同思路，
 * 但 AgentScope 用 {@link SkillBox} + {@link AgentSkill} 的<b>文本技能</b>注入。
 *
 * <p>本模块演示两个技能剧本：
 * <ul>
 *   <li><b>部署剧本</b>（deploy-playbook）：含部署步骤 + 专属工具 {@code executeDeploy}</li>
 *   <li><b>回滚剧本</b>（rollback-playbook）：含回滚步骤 + 专属工具 {@code executeRollback}</li>
 * </ul>
 *
 * <p>模型看到技能描述后，根据情境选择激活对应剧本。
 */
@Component
public class PlaybookAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public PlaybookAgent(
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
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(new DeployTools());
                    toolkit.registerTool(new RollbackTools());

                    AgentSkill deploySkill = AgentSkill.builder()
                            .name("deploy-playbook")
                            .description("部署剧本：当需要发布新版本时使用。先备份，再执行 deploy，最后验证。")
                            .skillContent("""
                                    # 部署剧本
                                    当用户要求部署时：
                                    1. 调用 backup 备份当前版本
                                    2. 调用 executeDeploy 发布新版本
                                    3. 调用 verifyDeployment 验证
                                    """)
                            .source("playbook")
                            .build();

                    AgentSkill rollbackSkill = AgentSkill.builder()
                            .name("rollback-playbook")
                            .description("回滚剧本：当部署失败需要回滚时使用。立即执行回滚并恢复备份。")
                            .skillContent("""
                                    # 回滚剧本
                                    当部署失败或用户要求回滚时：
                                    1. 调用 executeRollback 回滚
                                    2. 调用 restoreBackup 恢复备份
                                    """)
                            .source("playbook")
                            .build();

                    SkillBox skillBox = new SkillBox(toolkit);
                    skillBox.registerSkill(deploySkill);
                    skillBox.registerSkill(rollbackSkill);

                    local = HarnessAgent.builder()
                            .name("playbook-agent")
                            .sysPrompt("你是运维助手。你有两套剧本（deploy-playbook / rollback-playbook），"
                                    + "根据用户需求选择合适的剧本执行。")
                            .model(model)
                            .toolkit(toolkit)
                            .workspace(Paths.get(".agentscope/agentscope-playbook"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("playbook").userId("alice").build();
    }

    /** 部署工具。 */
    public static class DeployTools {
        @Tool(name = "backup", description = "备份当前版本")
        public String backup() { return "备份完成：backup-20240918.tar.gz"; }

        @Tool(name = "executeDeploy", description = "执行部署发布新版本")
        public String deploy() { return "部署成功：v2.1.0 已上线"; }

        @Tool(name = "verifyDeployment", description = "验证部署是否成功")
        public String verify() { return "验证通过：服务正常，响应 200"; }
    }

    /** 回滚工具。 */
    public static class RollbackTools {
        @Tool(name = "executeRollback", description = "执行回滚到上一版本")
        public String rollback() { return "回滚成功：已回到 v2.0.0"; }

        @Tool(name = "restoreBackup", description = "从备份恢复数据")
        public String restore() { return "恢复完成：数据已从 backup 恢复"; }
    }
}