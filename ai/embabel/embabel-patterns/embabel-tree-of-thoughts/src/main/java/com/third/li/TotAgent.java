package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * **思维树（Tree of Thoughts）**风格搜索。
 *
 * <p>与"一次让模型给答案"不同，这里把**搜索**显式做出来：
 * <ol>
 *   <li><b>分支</b>：先生成 3 个**互不相同**的解题思路</li>
 *   <li><b>评分</b>：让模型作为评审给每个思路打 0~1 分（可行性）</li>
 *   <li><b>展开**最优**</b>：只对得分最高的思路做 2 个改进版本（贪心扩展，控制成本）</li>
 *   <li><b>再评分并取全局最优</b>：返回最佳思路 + **完整搜索轨迹**（可解释）</li>
 * </ol>
 *
 * <p>为什么有用：复杂问题里模型的"第一反应"常常不是最优；显式分支 + 评分 + 剪枝
 * 能显著提升质量，代价是更多 LLM 调用（本示例 3 + 3 + 2 + 2 ≈ 10 次）。
 *
 * <p>与相近模式的区别：
 * <ul>
 *   <li>vs {@code embabel-parallelization}（sectioning）：分片是"同一任务的独立子任务"，
 *       这里是"同一问题的多个**竞争**方案"；</li>
 *   <li>vs {@code embabel-refinement}：refinement 是"沿一条路迭代改进"，
 *       ToT 是"先分叉、再按评分选路"。</li>
 * </ul>
 */
@Agent(description = "思维树：分支生成思路 -> 评分 -> 展开最优 -> 取全局最优")
public class TotAgent {

    private static final int BRANCH = 3;
    private static final int REFINEMENTS = 2;

    @Action(description = "搜索并返回最佳思路")
    @AchievesGoal(description = "产出最佳思路")
    public TotResult explore(UserInput userInput, Ai ai) {
        String problem = userInput.getContent();
        List<Candidate> explored = new ArrayList<>();

        // 1) 分支：生成互不相同的思路
        List<String> approaches = new ArrayList<>();
        for (int i = 0; i < BRANCH; i++) {
            approaches.add(ai.withDefaultLlm()
                    .withId("tot-gen-" + i)
                    .generateText("""
                            请针对下面的问题，给出第 %d 种**与其它方案明显不同**的解题思路。
                            只输出思路本身，一句话，30 字以内。

                            问题：%s
                            """.formatted(i + 1, problem)));
        }

        // 2) 评分
        List<Candidate> level1 = score(approaches, problem, ai, explored, "第1层");
        Candidate best = bestOf(level1);

        // 3) 展开最优
        List<String> refinements = new ArrayList<>();
        for (int i = 0; i < REFINEMENTS; i++) {
            refinements.add(ai.withDefaultLlm()
                    .withId("tot-refine-" + i)
                    .generateText("""
                            请在下面的思路基础上，给出第 %d 个**改进版本**（更具体、更可执行）。
                            只输出改进后的思路，一句话，30 字以内。

                            原思路：%s
                            问题：%s
                            """.formatted(i + 1, best.approach(), problem)));
        }

        // 4) 再评分并取全局最优
        List<Candidate> level2 = score(refinements, problem, ai, explored, "第2层");
        Candidate bestRefined = bestOf(level2);
        Candidate winner = bestRefined.score() >= best.score() ? bestRefined : best;

        return new TotResult(problem, winner.approach(), explored, 2);
    }

    private List<Candidate> score(
            List<String> approaches, String problem, Ai ai, List<Candidate> explored, String level) {
        List<Candidate> candidates = new ArrayList<>();
        for (int i = 0; i < approaches.size(); i++) {
            String approach = approaches.get(i);
            Judge judge = ai.withDefaultLlm()
                    .withId("tot-judge-" + level + "-" + i)
                    .creating(Judge.class)
                    .fromPrompt("""
                            请评估下面这个解题思路的**可行性与具体程度**，给出 0~1 的 score 和简短理由。

                            问题：%s
                            思路：%s
                            """.formatted(problem, approach));
            Candidate candidate = new Candidate(level, approach, judge.score(), judge.reason());
            candidates.add(candidate);
            explored.add(candidate);
        }
        return candidates;
    }

    private Candidate bestOf(List<Candidate> candidates) {
        return candidates.stream()
                .max(Comparator.comparingDouble(Candidate::score))
                .orElseThrow();
    }
}
