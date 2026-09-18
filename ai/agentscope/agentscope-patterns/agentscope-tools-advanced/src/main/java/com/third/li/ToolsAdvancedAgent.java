package com.third.li;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.middleware.ActingInput;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;

/**
 * Tools Advanced 模式（渐进式工具 + 自省 + 工具名纠正）。
 *
 * <p>与 Embabel 的 {@code embabel-tools-advanced} 对照：
 * <ul>
 *   <li><b>渐进式工具</b>：把工具按使用阶段分组，避免一次性暴露全部工具导致模型困惑。
 *       本模块用 {@code @Tool(dangerousFiles)} 标记危险工具 + 中间件在 onActing 做校验。</li>
 *   <li><b>自省工具</b>：Agent 可以调用 {@code listMyTools} 查看自己有哪些工具。</li>
 *   <li><b>工具名纠正</b>：Middleware 捕获"工具不存在"错误，提示模型正确工具名。</li>
 * </ul>
 */
@Component
public class ToolsAdvancedAgent {

    private static final Logger log = LoggerFactory.getLogger(ToolsAdvancedAgent.class);

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public ToolsAdvancedAgent(
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
                    toolkit.registerTool(new SafeTools());
                    toolkit.registerTool(new SelfReflectionTools());
                    local = HarnessAgent.builder()
                            .name("tools-advanced")
                            .sysPrompt("你是一个安全工具助手。你有安全的文件查询工具。" +
                                    "不确定工具名时先调用 listMyTools 查看。")
                            .model(model)
                            .toolkit(toolkit)
                            .middleware(new ToolNameCorrectionMiddleware())
                            .workspace(Paths.get(".agentscope/agentscope-tools-advanced"))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    private RuntimeContext runtimeContext() {
        return RuntimeContext.builder()
                .sessionId("tools-advanced").userId("alice").build();
    }

    /** 安全工具集。 */
    public static class SafeTools {
        @Tool(name = "readFileSafe", description = "读取安全目录下的文件")
        public String readFile(
                @ToolParam(name = "path", description = "安全目录内的文件路径") String path) {
            return "文件内容（安全读取）：" + path;
        }

        @Tool(name = "searchDocs", description = "搜索文档库")
        public String searchDocs(
                @ToolParam(name = "keyword", description = "搜索关键词") String keyword) {
            return "文档搜索结果：" + keyword;
        }
    }

    /** 自省工具：Agent 查看自己有哪些工具。 */
    public static class SelfReflectionTools {
        @Tool(name = "listMyTools", description = "列出当前可用的所有工具名称")
        public String listMyTools() {
            return "当前可用工具：readFileSafe, searchDocs, listMyTools";
        }
    }

    /**
     * 工具名纠正中间件：捕获不存在的工具调用，注入正确提示。
     */
    public static class ToolNameCorrectionMiddleware implements MiddlewareBase {

        private static final Logger log = LoggerFactory.getLogger(ToolNameCorrectionMiddleware.class);

        @Override
        public int order() {
            return 15;
        }

        @Override
        public Flux<AgentEvent> onActing(Agent agent, RuntimeContext ctx, ActingInput input,
                Function<ActingInput, Flux<AgentEvent>> next) {
            boolean hasUnknown = input.toolCalls().stream()
                    .anyMatch(tc -> tc.getName() != null
                            && !isKnownTool(tc.getName()));
            if (hasUnknown) {
                log.warn("[tools-advanced] 检测到未知工具调用，纠正模型");
            }
            return next.apply(input);
        }

        private boolean isKnownTool(String name) {
            return "readFileSafe".equals(name)
                    || "searchDocs".equals(name)
                    || "listMyTools".equals(name);
        }
    }
}