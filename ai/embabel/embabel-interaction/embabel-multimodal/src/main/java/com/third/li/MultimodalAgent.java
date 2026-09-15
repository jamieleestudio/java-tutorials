package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.AgentImage;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.MultimodalContent;
import com.embabel.agent.domain.io.UserInput;

import java.io.File;
import java.nio.file.Path;

/**
 * 多模态示例：让**视觉模型**描述图片。
 *
 * <p>要点：
 * <ul>
 *   <li>{@link AgentImage#fromFile(File)}（或 {@code fromPath}/{@code fromBytes}）把图片读成 {@code AgentImage}</li>
 *   <li>{@link MultimodalContent#withImage(String, AgentImage)} 把文本与图片组成一次多模态输入</li>
 *   <li>{@code ai.withLlm("qwen2.5vl:3b")} 指定支持视觉的模型（DeepSeek 目前没有视觉能力）</li>
 * </ul>
 *
 * <p>提示词与图片一起发给模型，模型即可"看图说话"。文档（PDF 等）同理，用
 * {@code AgentDocument} + {@code MultimodalContent.withDocument(...)}。
 */
@Agent(description = "多模态示例：用视觉模型描述图片内容")
public class MultimodalAgent {

    private final SampleImageGenerator sampleImages;

    public MultimodalAgent(SampleImageGenerator sampleImages) {
        this.sampleImages = sampleImages;
    }

    @Action(description = "描述图片内容")
    @AchievesGoal(description = "产出图片描述")
    public Description describe(UserInput userInput, Ai ai) {
        Path path = userInput.getContent().isBlank()
                ? sampleImages.sampleImage()
                : Path.of(userInput.getContent());

        AgentImage image = AgentImage.fromFile(path.toFile());

        String description = ai.withLlm("qwen2.5vl:3b")
                .withId("multimodal-describe")
                .generateText(MultimodalContent.withImage(
                        "请用中文描述这张图片：包含哪些形状、颜色，以及图上的文字是什么。",
                        image));

        return new Description(path.toString(), description);
    }
}
