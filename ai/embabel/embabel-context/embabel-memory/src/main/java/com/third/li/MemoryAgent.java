package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.identity.User;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * **跨会话长期记忆**：新会话开始时按"当前用户 + 本次问题"召回相关记忆，再作答。
 *
 * <p>流程：`问题向量化 → 按 userId 召回 top-k → 拼进提示词 → 回答`。
 *
 * <p>注意"按 userId 召回"这一步**是记忆与普通 RAG 的关键区别**：
 * 普通 RAG 检索的是"公共知识"，长期记忆检索的是"**这个用户**的历史"。
 * 忘了按 user 过滤就会串号（本模块把这一点写进 SQL 的 WHERE 里）。
 *
 * <p>用户身份来自 {@code OperationContext.user()}（由调用方通过
 * {@code ProcessOptions.withIdentities(...)} 传入）——这正是
 * {@code embabel-identity} 讲的机制，两个模块在这里组合起来用。
 */
@Agent(description = "跨会话长期记忆：召回用户偏好/事实后作答")
public class MemoryAgent {

    private static final Logger log = LoggerFactory.getLogger(MemoryAgent.class);

    private final MemoryStore store;

    public MemoryAgent(MemoryStore store) {
        this.store = store;
    }

    @Action(description = "带长期记忆作答")
    @AchievesGoal(description = "产出带记忆的回答")
    public MemoryAnswer answer(UserInput userInput, OperationContext context, Ai ai) {
        User user = context.user();
        String userId = user == null ? "anonymous" : user.getId();

        float[] query = ai.withDefaultEmbeddingService().embed(userInput.getContent());
        List<MemoryItem> recalled = store.recall(userId, query, 3);
        log.info("用户 {} 召回 {} 条记忆", userId, recalled.size());

        String memoryBlock = recalled.isEmpty()
                ? "（没有关于该用户的历史记忆）"
                : recalled.stream()
                        .map(item -> "- [%s] %s".formatted(item.kind(), item.text()))
                        .collect(Collectors.joining("\n"));

        String answer = ai.withDefaultLlm()
                .withId("memory-answer")
                .generateText("""
                        以下是关于当前用户的历史记忆（偏好与事实）。请在与问题相关时利用它们；
                        如果记忆与问题无关，就正常回答，不要生硬地提起记忆。

                        历史记忆：
                        %s

                        用户问题：%s
                        """.formatted(memoryBlock, userInput.getContent()));

        return new MemoryAnswer(userId, userInput.getContent(), recalled, answer);
    }
}
