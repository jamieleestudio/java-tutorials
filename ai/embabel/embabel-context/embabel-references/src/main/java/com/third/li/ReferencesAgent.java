package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.reference.LlmReference;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 引用加载示例（轻量 RAG）。
 *
 * <p>{@code LlmReference} 是"可注入的参考资料"：挂到 PromptRunner 上
 * （{@code withReferences(...)}）后，其 {@code contribution()} 会作为提示词的一部分，
 * 模型即可"依据资料"回答。它也可以携带工具（见 {@link DocumentReference} 的说明）。
 *
 * <p>本示例注入两份资料：
 * <ul>
 *   <li>classpath 文档 {@code docs/embabel-handbook.md}（模拟内部知识库）</li>
 *   <li>一段内联术语表</li>
 * </ul>
 * 与向量检索的区别：参考内容会**全量**进入提示词，只适合小体量知识；
 * 大知识库应使用 embedding + 向量库（Embabel 的 {@code EagerSearch} 接口）。
 */
@Agent(description = "引用加载示例：把内部文档与术语表作为参考资料注入后再回答")
public class ReferencesAgent {

    @Action(description = "依据参考资料回答问题")
    @AchievesGoal(description = "产出有依据的回答")
    public Answer answer(UserInput userInput, Ai ai) {
        LlmReference handbook = new DocumentReference(
                "embabel-handbook",
                "Embabel 核心概念与能力说明（内部文档）",
                readClasspath("docs/embabel-handbook.md"));

        LlmReference glossary = new DocumentReference(
                "glossary",
                "术语表",
                "Blackboard：动作之间按类型传递对象的共享上下文；"
                        + "GOAP：从目标反向规划动作链的规划器。");

        String answer = ai.withDefaultLlm()
                .withReferences(handbook, glossary)
                .withId("references-answer")
                .generateText("""
                        请只依据下方给出的参考资料回答问题（资料已包含在提示词中，无需调用任何工具）。
                        如果资料中没有答案，请明确说明"资料中未提及"，不要编造。
                        请在答案末尾标注使用到的参考资料名称。

                        问题：%s
                        """.formatted(userInput.getContent()));

        return new Answer(answer, List.of(handbook.getName(), glossary.getName()));
    }

    private String readClasspath(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read classpath resource: " + path, e);
        }
    }
}
