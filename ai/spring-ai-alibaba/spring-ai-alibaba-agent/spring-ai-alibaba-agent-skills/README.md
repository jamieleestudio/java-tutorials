# spring-ai-alibaba-agent-skills — 技能体系（ClasspathSkillRegistry + SkillsInterceptor）

## 演示内容

技能体系（ClasspathSkillRegistry + SkillsInterceptor）。

```bash
curl "http://localhost:8544/"
```

> classpath:skills/ 下每个子目录一个 SKILL.md 技能（frontmatter 元数据）。已运行验证。

## 运行

```bash
cd ai/spring-ai-alibaba
mvn -pl :spring-ai-alibaba-agent-skills spring-boot:run
```