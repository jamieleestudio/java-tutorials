package com.third.li;

import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.observability.HtmlReportGenerator;
import dev.langchain4j.observability.api.event.AiServiceEvent;
import dev.langchain4j.observability.api.event.AiServiceInvocationSummaryEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.stream.Collectors;

/** 可观测接口：GET /ai/agentic/monitor?topic=… 执行并返回调用树 + 导出 HTML 报告。 */
@RestController
public class AgenticObservabilityController {

    private final UntypedAgent monitoredWorkflow;
    private final AgentMonitor monitor;
    private final AgenticObservabilityConfig.Assistant summarizableAssistant;
    private final AgenticObservabilityConfig.CapturingSummaryListener summaryListener;

    public AgenticObservabilityController(UntypedAgent monitoredWorkflow, AgentMonitor monitor,
                                          AgenticObservabilityConfig.Assistant summarizableAssistant,
                                          AgenticObservabilityConfig.CapturingSummaryListener summaryListener) {
        this.monitoredWorkflow = monitoredWorkflow;
        this.monitor = monitor;
        this.summarizableAssistant = summarizableAssistant;
        this.summaryListener = summaryListener;
    }

    @GetMapping("/ai/agentic/monitor")
    public String run(@RequestParam(defaultValue = "Mastra 与 LangChain4j 的对比") String topic) {
        String result = (String) monitoredWorkflow.invoke(java.util.Map.of("topic", topic));
        String tree = monitor.successfulExecutions().isEmpty()
                ? "（无执行记录）"
                : String.valueOf(monitor.successfulExecutions().get(monitor.successfulExecutions().size() - 1));
        return "【最终输出】" + result + "\n\n【调用树】" + tree;
    }

    @GetMapping("/ai/agentic/monitor/report")
    public String report(@RequestParam(defaultValue = "LangChain4j 执行报告") String topic) {
        monitoredWorkflow.invoke(java.util.Map.of("topic", topic));
        Path target = Path.of("agentic-report.html");
        HtmlReportGenerator.generateReport(monitor, target);
        return "报告已生成：" + target.toAbsolutePath();
    }

    /** 1.23 新增：AI Service 一次调用的全部事件汇总。 */
    @GetMapping("/ai/agentic/summary")
    public String summary(@RequestParam(defaultValue = "什么是 RAG？") String question) {
        String answer = summarizableAssistant.answer(question);
        AiServiceInvocationSummaryEvent event = summaryListener.last();
        String detail = event == null
                ? "（未捕获到汇总事件）"
                : event.events().stream()
                        .map(AiServiceEvent::getClass)
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(" → "));
        return "【回答】" + answer
                + "\n\n【1.23 调用汇总事件流】" + detail
                + "\n【事件数】" + (event == null ? 0 : event.events().size());
    }
}
