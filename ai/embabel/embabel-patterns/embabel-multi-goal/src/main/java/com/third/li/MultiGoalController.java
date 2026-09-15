package com.third.li;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.domain.io.UserInput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多目标自动选择接口。
 *
 * <p>调用方**不指定**要哪个目标，只给输入；平台用**排序器**在 Agent 的多个目标里挑一个
 * （这里配置了 `embabel.agent.platform.ranking.llm`，由 LLM 排序）。
 * 返回里会带上"最终选中的目标"与产出，便于观察选择结果。
 *
 * <p><b>重要语义</b>（本模块踩坑记录）：{@code AgentInvocation.create(platform, X.class)} 里的
 * {@code X} 用来选 **Agent**（找第一个"有目标产出类型可赋给 X"的 Agent），
 * 并**不**用于在同一 Agent 的多个目标间路由；Agent 内部的目标由排序器决定。
 * 因此若要让调用方精确指定产出类型，可靠做法是**一个 Agent 一个目标**。
 */
@RestController
public class MultiGoalController {

    private final AgentPlatform agentPlatform;

    public MultiGoalController(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    @GetMapping("/multi-goal/auto")
    public GoalSelection auto(
            @RequestParam(value = "message", defaultValue = "引入 Agent 框架需要先做哪些准备？") String message) {
        // resultType = Any：任何目标都匹配，具体目标交给平台的排序器选择
        AgentProcess process = AgentInvocation.on(agentPlatform).run(new UserInput(message));

        return new GoalSelection(
                process.getGoal() == null ? "(未记录)" : process.getGoal().getName(),
                process.getStatus().name(),
                process.lastResult());
    }
}
