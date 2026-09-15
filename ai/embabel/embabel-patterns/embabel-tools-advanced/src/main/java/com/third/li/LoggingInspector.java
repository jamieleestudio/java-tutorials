package com.third.li;

import com.embabel.agent.api.tool.callback.AfterIterationContext;
import com.embabel.agent.api.tool.callback.AfterToolCallContext;
import com.embabel.agent.api.tool.callback.ToolCallInspector;
import com.embabel.agent.api.tool.callback.ToolLoopInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工具循环回调：观测（也可用于改写对话历史）。
 *
 * <p>两类回调：
 * <ul>
 *   <li>{@link ToolLoopInspector}：循环级事件（每次 LLM 调用前后、每次工具结果、每轮迭代结束）</li>
 *   <li>{@link ToolCallInspector}：单次工具调用前后</li>
 * </ul>
 * 它们都是只读观测；要**改写**对话历史/工具结果请用 {@code ToolLoopTransformer}
 * （{@code withToolLoopTransformers(...)}），例如做上下文压缩、窗口化。
 */
public class LoggingInspector implements ToolLoopInspector, ToolCallInspector {

    private static final Logger log = LoggerFactory.getLogger(LoggingInspector.class);

    @Override
    public void afterIteration(AfterIterationContext context) {
        log.info("[tool-loop] iteration finished: {}", context);
    }

    @Override
    public void afterToolCall(AfterToolCallContext context) {
        log.info("[tool-call] finished: {}", context);
    }
}
