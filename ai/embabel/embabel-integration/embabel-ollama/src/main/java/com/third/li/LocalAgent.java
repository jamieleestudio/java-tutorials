package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;

/**
 * **完全本地**的 Agent：模型跑在本地 Ollama 上，不需要任何云端 API Key。
 *
 * <p>引入 {@code embabel-agent-starter-ollama} 后：
 * <ul>
 *   <li>配置 {@code embabel.agent.platform.models.ollama.base-url} 指向 Ollama</li>
 *   <li>Embabel 会**自动发现** Ollama 里已安装的模型（调用其 {@code /api/tags}），
 *       无需手写模型清单（对比 OpenAI starter 需要 models yml）</li>
 *   <li>用 {@code embabel.models.default-llm} / {@code default-embedding-model} 指定默认模型</li>
 * </ul>
 *
 * <p>适用场景：数据不能出内网、离线环境、成本敏感的原型验证。
 * 代价是本地小模型的能力弱于云端大模型——可以按任务分级：
 * 简单任务走本地、复杂任务走云端（见 {@code embabel-multi-model} 的角色路由）。
 */
@Agent(description = "本地模型 Agent：用 Ollama 上的模型回答问题")
public class LocalAgent {

    @Action(description = "用本地模型回答")
    @AchievesGoal(description = "产出回答")
    public Reply answer(UserInput userInput, Ai ai) {
        String content = ai.withDefaultLlm()
                .withId("ollama-answer")
                .generateText("请用简洁的中文回答下面的问题：\n" + userInput.getContent());
        return new Reply(content);
    }
}
