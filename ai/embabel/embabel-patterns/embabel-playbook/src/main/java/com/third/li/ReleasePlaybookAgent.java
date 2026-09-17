package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.tool.Tool;
import com.embabel.agent.api.tool.agentic.playbook.PlaybookTool;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * **Playbook（解锁条件式工具集）**：工具按"前置依赖"逐步解锁。
 *
 * <p>用法是先声明依赖关系，再让手册自己推进：
 * <pre>
 *   new PlaybookTool("releasePlaybook", "上线检查手册")
 *       .withTools(rollback, unitTests)                          // 初始解锁
 *       .withTool(smokeTests).unlockedBy(unitTests)              // 跑完单测才可用
 *       .withTool(staging).unlockedByAll(unitTests, smokeTests)
 *       .withTool(production).unlockedBy(staging)
 * </pre>
 *
 * <p>与相近机制的区别（三者都是"按条件收敛工具集"，但收敛依据不同）：
 * <ul>
 *   <li>{@code embabel-state-machine}：按**显式状态枚举**收敛，转移由工具声明。</li>
 *   <li>{@code embabel-tool-chaining}：按**黑板上出现了什么对象**收敛（数据驱动）。</li>
 *   <li>{@code embabel-playbook}（本模块）：按**前置工具是否已执行**收敛（流程驱动），
 *       而且不要求预先定义状态机——只描述"谁在谁之后"。</li>
 * </ul>
 *
 * <p>为什么有用：把"流程约束"表达在工具可见性上，模型就不可能跳过前置步骤
 * （不是靠提示词叮嘱，而是"那个工具根本不在列表里"）。
 */
@Agent(description = "Playbook：上线检查手册，工具按前置依赖逐步解锁")
public class ReleasePlaybookAgent {

    private static final Logger log = LoggerFactory.getLogger(ReleasePlaybookAgent.class);

    @Action(description = "按手册推进上线流程")
    @AchievesGoal(description = "产出上线结果")
    public PlaybookOutcome release(UserInput userInput) {
        List<String> calls = Collections.synchronizedList(new ArrayList<>());

        Tool unitTests = step("runUnitTests", "运行单元测试", calls);
        Tool smokeTests = step("runSmokeTests", "运行冒烟测试", calls);
        Tool staging = step("deployToStaging", "部署到预发环境", calls);
        Tool production = step("deployToProduction", "部署到生产环境", calls);
        Tool rollback = step("rollback", "回滚（应急工具，始终可用）", calls);

        PlaybookTool playbook = new PlaybookTool("releasePlaybook", "上线检查手册：工具按依赖逐步解锁")
                // 初始解锁：应急回滚 + 第一步
                .withTools(rollback, unitTests)
                // 锁定：按前置依赖逐步解锁
                .withTool(smokeTests).unlockedBy(unitTests)
                .withTool(staging).unlockedByAll(unitTests, smokeTests)
                .withTool(production).unlockedBy(staging);

        log.info("手册初始状态：解锁 {} 个，锁定 {} 个",
                playbook.getUnlockedToolCount(), playbook.getLockedToolCount());

        // 直接驱动手册（而不是再套一层 LLM 工具循环）：
        // PlaybookTool 本身就是 AgenticTool，内部有自己的 LLM 循环；
        // 外层再套一层只会让模型看到"一个返回总结的黑盒"，反而困惑。
        Tool.Result result = playbook.call(userInput.getContent());
        String summary = result instanceof Tool.Result.Text text ? text.getContent() : result.toString();

        return new PlaybookOutcome(
                summary,
                List.copyOf(calls),
                playbook.getUnlockedToolCount(),
                playbook.getLockedToolCount(),
                "初始解锁 rollback 与 runUnitTests；冒烟测试需先跑单测，"
                        + "预发需单测+冒烟都跑过，生产需先部署预发。");
    }

    private Tool step(String name, String description, List<String> calls) {
        return Tool.create(name, description, args -> {
            calls.add(name);
            log.info("  [playbook] {}", name);
            return Tool.Result.text("%s 已完成".formatted(description));
        });
    }
}
