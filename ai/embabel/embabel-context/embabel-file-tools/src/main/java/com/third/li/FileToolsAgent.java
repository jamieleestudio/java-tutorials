package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.tools.file.FileTools;
import org.springframework.beans.factory.annotation.Value;

/**
 * 文件工具示例。
 *
 * <p>{@link FileTools} 提供一套开箱即用的文件工具（{@code @LlmTool} 方法）：
 * 读取/列出/查找文件、创建/写入/编辑/追加/删除文件等。
 * 两种工厂：
 * <ul>
 *   <li>{@code FileTools.readOnly(root)}：只读（推荐先从这个开始）</li>
 *   <li>{@code FileTools.readWrite(root)}：可写，本示例使用它演示"让模型创建文件"</li>
 * </ul>
 *
 * <p>所有路径都相对 {@code root} 解析并做越界校验，因此务必把 root 指向一个**沙箱目录**。
 */
@Agent(description = "文件工具示例：在沙箱目录内读写、搜索文件")
public class FileToolsAgent {

    private final String sandboxDir;

    public FileToolsAgent(@Value("${demo.sandbox-dir}") String sandboxDir) {
        this.sandboxDir = sandboxDir;
    }

    @Action(description = "使用文件工具完成用户的文件操作请求")
    @AchievesGoal(description = "返回文件操作结果")
    public ChatReply chat(UserInput userInput, Ai ai) {
        FileTools tools = FileTools.readWrite(sandboxDir);

        String answer = ai.withDefaultLlm()
                .withTools(Tool.fromInstance(tools))
                .withId("file-tools")
                .generateText("""
                        你可以使用提供的文件工具，在沙箱目录内读写文件。
                        请先列出目录确认现状，再执行用户请求，最后简要说明你做了哪些操作。

                        用户请求：%s
                        """.formatted(userInput.getContent()));
        return new ChatReply(answer);
    }
}
