package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 工具分组（ToolGroup + group registration）。
 *
 * <p>AgentScope 的工具分组通过 {@link Toolkit.ToolRegistration#group(String)}
 * 把工具划入命名组，然后由 {@code SkillBox.syncToolGroupStates()} 根据激活的
 * 技能自动切换工具组的激活/停用。
 *
 * <p>本模块演示两种用法：
 * <ul>
 *   <li>只读工具组（read-only）：搜索、查询 —— 始终激活</li>
 *   <li>写入工具组（write）：创建、删除 —— 按需激活</li>
 * </ul>
 */
@Component
public class ToolGroupAgent {

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public ToolGroupAgent(
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

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel model = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    Toolkit toolkit = new Toolkit();
                    toolkit.registration()
                            .tool(new ReadOnlyTools())
                            .group("read-only")
                            .apply();
                    toolkit.registration()
                            .tool(new WriteTools())
                            .group("write")
                            .apply();
                    local = HarnessAgent.builder()
                            .name("toolkit-groups")
                            .sysPrompt("你是一个文件管理助手。你有只读工具和写入工具。只读工具始终可用，写入工具按需使用。")
                            .model(model)
                            .toolkit(toolkit)
                            .workspace(java.nio.file.Paths.get(".agentscope/workspace-toolkit-groups"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("toolgroup-demo").userId("alice").build();
    }

    /** 只读工具：搜索和查询。 */
    public static class ReadOnlyTools {
        @io.agentscope.core.tool.Tool(name = "search_files",
                description = "搜索文件内容，返回匹配的文件名和行号",
                readOnly = true)
        public String searchFiles(
                @io.agentscope.core.tool.ToolParam(name = "pattern", description = "搜索关键词") String pattern) {
            return "找到 3 个匹配文件：README.md, pom.xml, Application.java";
        }

        @io.agentscope.core.tool.Tool(name = "list_files",
                description = "列出目录下的文件",
                readOnly = true)
        public String listFiles(
                @io.agentscope.core.tool.ToolParam(name = "directory", description = "目录路径") String directory) {
            return directory + "/ 下有：file1.txt, file2.txt, file3.txt";
        }
    }

    /** 写入工具：创建和删除。 */
    public static class WriteTools {
        @io.agentscope.core.tool.Tool(name = "create_file",
                description = "创建一个新文件")
        public String createFile(
                @io.agentscope.core.tool.ToolParam(name = "path", description = "文件路径") String path,
                @io.agentscope.core.tool.ToolParam(name = "content", description = "文件内容") String content) {
            return "已创建文件：" + path + "（" + content.length() + " 字节）";
        }

        @io.agentscope.core.tool.Tool(name = "delete_file",
                description = "删除一个文件")
        public String deleteFile(
                @io.agentscope.core.tool.ToolParam(name = "path", description = "文件路径") String path) {
            return "已删除文件：" + path;
        }
    }
}