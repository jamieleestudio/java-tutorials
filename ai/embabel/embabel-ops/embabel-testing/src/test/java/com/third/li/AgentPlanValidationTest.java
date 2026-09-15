package com.third.li;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.scope.AgentScopeBuilder;
import com.embabel.agent.core.AgentScope;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.spi.validation.GoapPathToCompletionValidator;
import com.embabel.common.core.validation.ValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 规划校验测试：**不需要 API Key、不需要启动 Spring**。
 *
 * <p>{@link GoapPathToCompletionValidator} 会分析 Agent 的动作/目标/类型依赖，
 * 判断"从初始输入能否规划出一条到达目标的路径"。这类测试能在几毫秒内
 * 发现"动作链断了""目标没有动作产出"等设计错误。
 */
class AgentPlanValidationTest {

    @Test
    void wellFormedAgent_canReachItsGoal() {
        AgentScope scope = AgentScopeBuilder.fromInstance(new OutlineAgent()).createAgentScope();

        ValidationResult result = new GoapPathToCompletionValidator().validate(scope);

        assertTrue(result.isValid(), () -> "expected a valid plan but got: " + result.getErrors());
    }

    @Test
    void brokenAgent_hasNoPathToGoal() {
        AgentScope scope = AgentScopeBuilder.fromInstance(new BrokenAgent()).createAgentScope();

        ValidationResult result = new GoapPathToCompletionValidator().validate(scope);

        assertFalse(result.isValid(), "expected validation errors for a cyclic action graph");
    }

    /**
     * 故意写坏的 Agent：两个动作互相依赖（A 需要 B、B 需要 A），形成环，
     * 因此**没有任何动作可以作为起始动作**，规划器找不到到达目标的路径。
     *
     * <p>注意：如果只是"某个输入没有动作产出"，校验器会把它当作**外部输入**（假定可用），
     * 并不算错误——这是理解该校验器行为的关键。
     */
    @Agent(description = "故意不可达的 Agent：动作互相依赖形成环")
    static class BrokenAgent {

        @Action(description = "需要 TypeB 才能产出 TypeA")
        public TypeA produceA(TypeB b) {
            return new TypeA("a");
        }

        @Action(description = "需要 TypeA 才能产出 TypeB（目标动作）")
        @AchievesGoal(description = "产出 TypeB")
        public TypeB produceB(TypeA a) {
            return new TypeB("b");
        }
    }

    record TypeA(String value) {
    }

    record TypeB(String value) {
    }
}
