package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 可观测性接入点接口。 */
@RestController
public class OtelController {

    private final AgentPlatform agentPlatform;
    private final SpanRecorder recorder;

    public OtelController(AgentPlatform agentPlatform, SpanRecorder recorder) {
        this.agentPlatform = agentPlatform;
        this.recorder = recorder;
    }

    /** 跑一次 Agent，并返回这次产生的 span。 */
    @GetMapping("/observability/run")
    public SpanReport run(
            @RequestParam(value = "message", defaultValue = "为什么 Agent 需要预算与熔断") String message) {
        recorder.clear();
        TracedAgent.Summary summary = AgentInvocation.create(agentPlatform, TracedAgent.Summary.class)
                .invoke(new UserInput(message));
        List<SpanReport.Span> spans = recorder.spans();
        return new SpanReport(
                summary.text(),
                spans.size(),
                spans,
                "这些 span 由框架在进程/动作执行点自动开启；换成 OTLP exporter 即可导出到 Jaeger/Tempo，"
                        + "Agent 代码无需改动。");
    }

    /** 只读当前收集到的 span（不清空）。 */
    @GetMapping("/observability/spans")
    public List<SpanReport.Span> spans() {
        return recorder.spans();
    }
}
