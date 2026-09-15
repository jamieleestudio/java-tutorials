package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrator-workers（编排者-工人）模式。
 *
 * <p>对应 Anthropic《Building Effective Agents》里的 *Orchestrator-workers*：
 * 由一个**中央 LLM 动态拆解任务**（子任务数量事先不可知），派给 worker 执行，再综合结果。
 *
 * <p>三步：
 * <ol>
 *   <li>{@code decompose}：编排者**动态**产出子任务列表（2~4 个，由模型决定）</li>
 *   <li>{@code work}：worker 逐个执行子任务（本例在一个动作里循环；也可用 ScatterGather 并行）</li>
 *   <li>{@code synthesize}：综合所有结论，产出最终报告</li>
 * </ol>
 *
 * <p>与相近模式的区别：
 * <ul>
 *   <li>vs {@code embabel-supervisor}：supervisor 是"LLM 逐轮决定调哪个**已有动作**"；
 *       orchestrator 是"先把任务**拆成新子任务**"。</li>
 *   <li>vs {@code embabel-parallelization}：sectioning 的子任务**预定义**且固定；
 *       orchestrator 的子任务**运行期才确定**（这正是文章强调的关键差异）。</li>
 * </ul>
 */
@Agent(description = "编排者-工人：动态拆解子任务 -> 逐个执行 -> 综合")
public class OrchestratorAgent {

    @Action(description = "编排者：动态拆解子任务")
    public Subtasks decompose(UserInput userInput, Ai ai) {
        return ai.withDefaultLlm()
                .creating(Subtasks.class)
                .fromPrompt("""
                        请把下面的任务拆解成 2~4 个可以独立调研的子任务，每个子任务一句话。
                        只输出子任务列表。

                        任务：%s
                        """.formatted(userInput.getContent()));
    }

    @Action(description = "工人：逐个执行子任务")
    public WorkerResults work(Subtasks subtasks, UserInput userInput, Ai ai) {
        List<WorkerResult> results = new ArrayList<>();
        for (String subtask : subtasks.subtasks()) {
            String finding = ai.withDefaultLlm()
                    .withId("worker-" + results.size())
                    .generateText("""
                            你是执行子任务的工人。请针对下面的子任务给出简明结论（100 字以内）。

                            总任务：%s
                            子任务：%s
                            """.formatted(userInput.getContent(), subtask));
            results.add(new WorkerResult(subtask, finding));
        }
        return new WorkerResults(results);
    }

    @Action(description = "综合所有 worker 的结论")
    @AchievesGoal(description = "产出最终报告")
    public FinalReport synthesize(WorkerResults results, UserInput userInput, Ai ai) {
        String joined = results.results().stream()
                .map(result -> "- 【%s】%s".formatted(result.subtask(), result.finding()))
                .reduce("", (a, b) -> a + b + "\n");

        String content = ai.withDefaultLlm()
                .withId("orchestrator-synthesize")
                .generateText("""
                        请把下面的子任务结论综合成一份最终报告（含结论与建议）。

                        原始任务：%s
                        子任务结论：
                        %s
                        """.formatted(userInput.getContent(), joined));

        return new FinalReport(content, results.results().size());
    }
}
