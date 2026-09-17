package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.skill.WorkspaceSkillRepository;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;

/**
 * 技能系统（SkillBox + AgentSkill + WorkspaceSkillRepository）。
 *
 * <p>AgentScope 的技能系统分三层：
 * <ol>
 *   <li>{@link AgentSkill} — 一个技能定义（name + description + skillContent + resources）</li>
 *   <li>{@link io.agentscope.core.skill.repository.AgentSkillRepository} — 技能存储
 *       （{@link WorkspaceSkillRepository} 从文件系统加载）</li>
 *   <li>{@link SkillBox} — 把技能注入到 Agent 的系统提示词中</li>
 * </ol>
 *
 * <p>技能内容（skillContent）是一段 Markdown 文本，会被自动拼接到系统提示中。
 * 模型"看到"技能描述后，可以选择激活技能——激活后技能的工具组会被启用。
 *
 * <p>与 Embabel 的差异：Embabel 的"技能"是 @Action 注解的方法，
 * AgentScope 的技能是<b>文本内容</b>（prompt-level），不是代码——
 * 更接近 Claude Code 的 SKILL.md 模式。
 */
@Component
public class SkillAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;
    private volatile WorkspaceSkillRepository skillRepo;

    public SkillAgent(
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

    /** 列出已注册的技能。 */
    public String listSkills() {
        List<String> names = skillRepo().getAllSkillNames();
        if (names.isEmpty()) {
            return "当前无技能（请先调用 /skills/seed 注入示例技能）";
        }
        return "已注册技能：" + String.join(", ", names);
    }

    /** 注入一个示例技能。 */
    public String seedSkill() {
        AgentSkill skill = AgentSkill.builder()
                .name("git-workflow")
                .description("Git 工作流技能：提交、分支、合并的标准操作流程")
                .skillContent("""
                        # Git 工作流

                        当用户要求提交代码时，按以下步骤操作：
                        1. `git add -A`
                        2. `git commit -m "<描述>"`
                        3. `git push`

                        如果有冲突，先 `git pull --rebase` 再 push。
                        """)
                .source("manual")
                .build();
        skillRepo().save(List.of(skill), false);
        return "已注入技能：" + skill.getName();
    }

    private WorkspaceSkillRepository skillRepo() {
        WorkspaceSkillRepository local = skillRepo;
        if (local == null) {
            synchronized (this) {
                local = skillRepo;
                if (local == null) {
                    var wsPath = Paths.get(".agentscope/workspace-skills");
                    wsPath.toFile().mkdirs();
                    LocalFilesystem fs = new LocalFilesystem(wsPath);
                    local = new WorkspaceSkillRepository(fs, "skills",
                            () -> RuntimeContext.builder().sessionId("skills").userId("alice").build());
                    skillRepo = local;
                }
            }
        }
        return local;
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
                            .name("skills-agent")
                            .sysPrompt("你是一个乐于助人的助手。你可以使用技能来增强你的能力。")
                            .model(model)
                            .skillRepository(skillRepo())
                            .workspace(Paths.get(".agentscope/workspace-skills"))
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
}