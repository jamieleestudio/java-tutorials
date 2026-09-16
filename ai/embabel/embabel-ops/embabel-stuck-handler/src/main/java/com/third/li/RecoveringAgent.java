package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.StuckHandler;
import com.embabel.agent.api.common.StuckHandlerResult;
import com.embabel.agent.api.common.StuckHandlingResultCode;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * **用 StuckHandler 兜底卡住的进程**。
 *
 * <p>注册方式很特别：**让 {@code @Agent} 注解的类自己实现 {@link StuckHandler}**。
 * 框架在读取 Agent 元数据时用 {@code instance as? StuckHandler} 认出来，
 * 挂到该 Agent 的 {@code stuckHandler} 上（见 {@code AgentMetadataReader}）。
 *
 * <p>本模块的 Agent 故意做成"规划不通"：目标动作 {@link #write} 需要 {@link Analysis}，
 * 但**没有任何动作产出 Analysis**，所以规划器找不到路径 → 进程 STUCK →
 * 框架回调 {@link #handleStuck}。
 *
 * <p>解除卡住的方式是**对 {@code AgentProcess} 施加副作用**（这里往黑板补一个 Analysis），
 * 然后返回 {@link StuckHandlingResultCode#REPLAN}，框架会重新规划并继续执行。
 * 如果补不上，返回 {@code NO_RESOLUTION}，进程就停在 STUCK。
 *
 * <p>与相近机制的区别：
 * <ul>
 *   <li>vs {@code embabel-replanning}：那里是**工具主动报告失败**后换路（{@code Tool.replanWhen}）；
 *       这里是**规划器自己找不到路**时的兜底钩子，触发时机完全不同。</li>
 *   <li>vs {@code embabel-budget}：预算是"超限就终止"；StuckHandler 是"卡住就补救"。</li>
 * </ul>
 */
@Agent(description = "演示 StuckHandler：规划器卡住时补上缺失前提并自动重规划")
public class RecoveringAgent implements StuckHandler {

    private static final Logger log = LoggerFactory.getLogger(RecoveringAgent.class);

    @Action(description = "根据分析要点写报告")
    @AchievesGoal(description = "产出报告")
    public Report write(Analysis analysis, Ai ai) {
        return new Report(ai.withDefaultLlm()
                .withId("stuck-write")
                .generateText("根据下面的分析要点，写一段 150 字以内的报告：\n" + analysis.points()));
    }

    /**
     * 卡住时的补救：往黑板补上缺失的 {@link Analysis}，然后请求重规划。
     *
     * <p>注意这里必须**有终止条件**：补过一次之后 {@code last(Analysis.class)} 就不为空了，
     * 再卡住就返回 {@code NO_RESOLUTION}，否则会无限重规划。
     */
    @Override
    public StuckHandlerResult handleStuck(AgentProcess process) {
        if (process.last(Analysis.class) == null) {
            process.addObject(new Analysis("（由 StuckHandler 补齐）需求真实、技术可行、成本可控、风险可接受"));
            log.info("StuckHandler 已向黑板补齐缺失的 Analysis，请求重规划");
            return new StuckHandlerResult(
                    "已补齐缺失的 Analysis，请求重规划",
                    this,
                    StuckHandlingResultCode.REPLAN,
                    process);
        }
        log.warn("StuckHandler 无法解决：缺失的不是 Analysis");
        return new StuckHandlerResult(
                "缺失的不是 Analysis，无法解决",
                this,
                StuckHandlingResultCode.NO_RESOLUTION,
                process);
    }
}
