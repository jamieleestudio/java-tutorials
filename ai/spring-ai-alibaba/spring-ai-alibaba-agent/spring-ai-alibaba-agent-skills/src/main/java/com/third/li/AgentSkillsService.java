package com.third.li;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.interceptor.skills.SkillsInterceptor;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * 技能体系：SkillRegistry + SkillsInterceptor。
 *
 * <p>SAA 的"技能"是带元数据的提示词包（{@code SKILL.md} 格式，与 Claude Code skills 同构）：
 * <ul>
 *   <li>{@link ClasspathSkillRegistry} / FileSystemSkillRegistry — 扫描技能目录，
 *       解析每个技能的元数据（名称/描述/正文）</li>
 *   <li>{@link SkillsInterceptor} — 模型调用拦截器：把可用技能清单注入系统提示，
 *       并挂载 {@code read_skill} 工具让模型按需读取技能全文</li>
 * </ul>
 *
 * <p>对照 {@code agentscope-skills}（SkillBox + AgentSkill）：同一范式，SAA 用
 * Markdown + frontmatter 承载技能定义。
 */
@Service
public class AgentSkillsService {

    private final ReactAgent agent;

    public AgentSkillsService(ChatModel chatModel) throws GraphStateException {
        // classpath:skills/ 下的每个子目录是一个技能（含 SKILL.md）
        var registry = ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .basePath(new ClassPathResource("skills").getPath())
                .autoLoad(true)
                .build();

        this.agent = ReactAgent.builder()
                .name("skills-agent")
                .description("带技能库的写作助手")
                .systemPrompt("你是写作助手。遇到与技能相关的任务时，先用 read_skill 工具阅读对应技能再执行。")
                .model(chatModel)
                .interceptors(SkillsInterceptor.builder()
                        .skillRegistry(registry)
                        .build())
                .build();
    }

    public String ask(String message) throws Exception {
        return agent.call(message).getText();
    }
}
