package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parallelization - **Voting**（投票）：把**同一个任务**跑多次（不同角度/提示词），
 * 再按多数票或阈值汇总，以提高置信度。
 *
 * <p>对应文章里 Parallelization 的第二个变体。例子：让多个提示词分别审查同一段代码是否
 * 有漏洞，或对"内容是否不当"做多轮判定并设置不同投票阈值来平衡误报/漏报。
 *
 * <p>这里用三个不同视角（严格审查者 / 用户视角 / 合规视角）各做一次独立判定，
 * 然后按多数票得出结论。
 */
@Agent(description = "Voting：同一任务多视角独立判定后投票")
public class VotingAgent {

    private static final List<String> ANGLES = List.of(
            "以严格的代码审查者视角",
            "以普通用户视角",
            "以合规与风控视角");

    @Action(description = "多视角独立判定后投票")
    @AchievesGoal(description = "产出判定结论")
    public Verdict vote(UserInput userInput, Ai ai) {
        List<String> votes = new ArrayList<>();
        for (String angle : ANGLES) {
            String vote = ai.withDefaultLlm()
                    .withId("vote-" + votes.size())
                    .generateText("""
                            请%s判断下面的内容是否存在问题。
                            只回答"有问题"或"没问题"，不要解释。

                            内容：%s
                            """.formatted(angle, userInput.getContent()));
            votes.add(normalize(vote));
        }

        long problemVotes = votes.stream().filter("有问题"::equals).count();
        String decision = problemVotes * 2 > votes.size() ? "有问题" : "没问题";

        return new Verdict(
                decision,
                votes,
                "共 %d 票，其中 %d 票认为有问题，按多数票判定：%s"
                        .formatted(votes.size(), problemVotes, decision));
    }

    private String normalize(String raw) {
        String text = raw == null ? "" : raw.toUpperCase(Locale.ROOT);
        return text.contains("没问题") || text.contains("NO") ? "没问题" : "有问题";
    }
}
