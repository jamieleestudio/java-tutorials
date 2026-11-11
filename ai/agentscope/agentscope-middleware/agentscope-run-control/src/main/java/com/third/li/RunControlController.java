package com.third.li;

import io.agentscope.core.agent.AgentRun;
import io.agentscope.core.message.Msg;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 运行控制接口（AgentScope 2.0.4 新特性）。
 *
 * <p>POST /runcontrol/start 创建一次运行并异步驱动，随后可以随时：
 * <ul>
 *   <li>GET  /runcontrol/status  查看运行状态</li>
 *   <li>GET  /runcontrol/result  查看已产出内容</li>
 *   <li>POST /runcontrol/cancel  取消该次运行</li>
 *   <li>POST /runcontrol/interrupt 中断并插话</li>
 * </ul>
 * 每次 start 都是独立的 {@link AgentRun}：取消一次不影响其它会话。
 */
@RestController
public class RunControlController {

    private final RunControlAgent runControlAgent;
    private final Map<String, ManagedRun> runs = new ConcurrentHashMap<>();

    public RunControlController(RunControlAgent runControlAgent) {
        this.runControlAgent = runControlAgent;
    }

    /** 创建并异步驱动一次运行。 */
    @PostMapping("/runcontrol/start")
    public Map<String, Object> start(
            @RequestParam(defaultValue = "请详细解释什么是 GOAP 规划，尽量长一些，方便测试取消") String message) {
        AgentRun<Msg> run = runControlAgent.prepare(message);
        StringBuilder collected = new StringBuilder();
        run.stream()
                .subscribe(
                        chunk -> collected.append(chunk.getTextContent()),
                        err -> { /* 终止状态见 termination */ });
        runs.put(run.runId(), new ManagedRun(run, collected));
        return Map.of("runId", run.runId(), "hint", "用 /runcontrol/cancel?runId=… 可随时取消");
    }

    @GetMapping("/runcontrol/status")
    public Map<String, Object> status(@RequestParam String runId) {
        ManagedRun managed = runs.get(runId);
        if (managed == null) {
            return Map.of("runId", runId, "error", "runId 不存在");
        }
        return Map.of("runId", runId, "status", String.valueOf(managed.run.status()));
    }

    @GetMapping("/runcontrol/result")
    public Map<String, Object> result(@RequestParam String runId) {
        ManagedRun managed = runs.get(runId);
        if (managed == null) {
            return Map.of("runId", runId, "error", "runId 不存在");
        }
        return Map.of("runId", runId, "partial", managed.collected().toString());
    }

    /** 取消运行：2.0.4 支持单独取消某个运行中的调用。 */
    @PostMapping("/runcontrol/cancel")
    public Map<String, Object> cancel(@RequestParam String runId) {
        ManagedRun managed = runs.get(runId);
        if (managed == null) {
            return Map.of("runId", runId, "error", "runId 不存在");
        }
        return Map.of("runId", runId, "cancelled", managed.run.cancel());
    }

    /** 中断并插入提示。 */
    @PostMapping("/runcontrol/interrupt")
    public Map<String, Object> interrupt(@RequestParam String runId,
                                         @RequestParam(defaultValue = "请换一种更简洁的说法") String note) {
        ManagedRun managed = runs.get(runId);
        if (managed == null) {
            return Map.of("runId", runId, "error", "runId 不存在");
        }
        return Map.of("runId", runId, "interrupted", managed.run.interrupt());
    }

    /** 对照：returnDirect=true 的 now 工具，直接短路返回，不经过第二轮推理。 */
    @GetMapping("/runcontrol/direct-tool")
    public Map<String, Object> directTool() {
        return Map.of("answer", runControlAgent.chat("调用 now 工具告诉我现在几点"));
    }

    private record ManagedRun(AgentRun<Msg> run, StringBuilder collected) {
    }
}
