package com.third.li;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.agent.config.FailoverListener;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;

/**
 * 运行级控制（AgentScope 2.0.4 新特性）。
 *
 * <p>与 {@code agent.call(...)}（阻塞到结束）不同，2.0.4 的
 * {@code prepareCall / prepareRun} 返回 {@link io.agentscope.core.agent.AgentRun}：
 * <ul>
 *   <li>{@code runId()} —— 每次运行有独立 ID；{@code status()} / {@code termination()} 查询状态</li>
 *   <li>{@code cancel()} —— 取消正在排队/运行中的这次调用（不影响其它 session）</li>
 *   <li>{@code interrupt(msg)} —— 中断并插入提示</li>
 * </ul>
 * {@code RuntimeContext} 显式传入（2.0.4 移除了隐式的 activeRc / RuntimeContextAware）。
 *
 * <p>同时演示 2.0.4 的另外两个新特性：
 * <ul>
 *   <li>{@code @Tool(returnDirect = true)}：工具结果直接短路推理循环，不再回给模型</li>
 *   <li>{@code fallbackModel + failoverListener}：主模型失败时自动回退，并收到
 *       {@link FailoverListener#onFailover(Model, Throwable)} 回调</li>
 * </ul>
 */
@Component
public class RunControlAgent {

    private static final Logger log = LoggerFactory.getLogger(RunControlAgent.class);

    private static final String WORKSPACE_DIR = ".agentscope/workspace-run-control";

    private final String modelName;
    private final String apiKey;
    private final String baseUrl;
    private volatile HarnessAgent agent;

    public RunControlAgent(
            @Value("${agentscope.model.name:deepseek-v4-flash}") String modelName,
            @Value("${agentscope.model.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${agentscope.model.base-url:https://api.deepseek.com}") String baseUrl) {
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    private HarnessAgent agent() {
        HarnessAgent local = agent;
        if (local == null) {
            synchronized (this) {
                local = agent;
                if (local == null) {
                    OpenAIChatModel primary = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
                    OpenAIChatModel fallback = OpenAIChatModel.builder()
                            .apiKey(apiKey).modelName("deepseek-reasoner").baseUrl(baseUrl).build();

                    FailoverListener failoverListener = (model, throwable) ->
                            log.warn("模型回退触发：{} 失败（{}），切换到备用模型",
                                    model.getModelName(), throwable.getMessage());

                    Toolkit toolkit = new Toolkit();
                    toolkit.registerTool(new DirectTools());

                    local = HarnessAgent.builder()
                            .name("run-control")
                            .sysPrompt("你是一个乐于助人的中文智能助手。")
                            .model(primary)
                            .fallbackModel(fallback)
                            .failoverListener(failoverListener)
                            .toolkit(toolkit)
                            .workspace(Paths.get(WORKSPACE_DIR))
                            .build();
                    agent = local;
                }
            }
        }
        return local;
    }

    /** returnDirect 工具：结果直接作为最终答复返回，不进入下一轮推理。 */
    public static class DirectTools {

        @Tool(name = "now", description = "获取服务器当前时间", returnDirect = true)
        public String now() {
            return "服务器当前时间：" + java.time.LocalDateTime.now();
        }
    }

    /** 创建一次可取消/可中断的运行（不订阅不执行）。 */
    public io.agentscope.core.agent.AgentRun<Msg> prepare(String message) {
        return agent().prepareCall(
                List.of(new UserMessage(message)),
                RuntimeContext.builder().sessionId("run-control-demo").userId("alice").build());
    }

    /** 直接调用（对照 prepareCall 的阻塞路径）。 */
    public String chat(String message) {
        return agent().call(new UserMessage(message),
                        RuntimeContext.builder().sessionId("run-control-demo").userId("alice").build())
                .block().getTextContent();
    }
}
