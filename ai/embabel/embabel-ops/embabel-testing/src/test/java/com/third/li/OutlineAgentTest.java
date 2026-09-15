package com.third.li;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.agent.domain.io.UserInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 动作单测：用 Mockito 模拟 {@code Ai} 与 {@code PromptRunner}，
 * 不触网、不消耗额度，几毫秒即可跑完。
 *
 * <p>价值点：除了断言返回值，还能用 {@link ArgumentCaptor} 捕获真实发出的提示词，
 * 验证"用户输入被正确拼进了 prompt"。
 */
@ExtendWith(MockitoExtension.class)
class OutlineAgentTest {

    @Mock
    private Ai ai;

    @Mock
    private PromptRunner promptRunner;

    @Mock
    private PromptRunner.Creating<TalkingPoints> creating;

    @Test
    void outline_returnsLlmResultAndBuildsPromptFromUserInput() {
        when(ai.withDefaultLlm()).thenReturn(promptRunner);
        when(promptRunner.creating(TalkingPoints.class)).thenReturn(creating);
        when(creating.fromPrompt(anyString()))
                .thenReturn(new TalkingPoints(List.of("要点一", "要点二", "要点三")));

        TalkingPoints result = new OutlineAgent().outline(new UserInput("为什么要做类型化建模？"), ai);

        assertEquals(3, result.points().size());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(creating).fromPrompt(promptCaptor.capture());
        assertTrue(promptCaptor.getValue().contains("为什么要做类型化建模？"),
                () -> "prompt should contain the user input, but was: " + promptCaptor.getValue());
    }
}
