package com.third.li;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.PromptRunner;
import com.embabel.agent.domain.io.UserInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ChatAgent 单元测试：使用 Mockito 模拟 Embabel 的 Ai 网关，无需真实 API Key。
 */
@ExtendWith(MockitoExtension.class)
class ChatAgentTest {

    @Mock
    private Ai ai;

    @Mock
    private PromptRunner promptRunner;

    @Test
    void chat_returnsAnswerFromLlm() {
        when(ai.withDefaultLlm()).thenReturn(promptRunner);
        when(promptRunner.withId(anyString())).thenReturn(promptRunner);
        when(promptRunner.generateText(anyString())).thenReturn("这是一个测试回答");

        ChatReply reply = new ChatAgent().chat(new UserInput("你好"), ai);

        assertEquals("这是一个测试回答", reply.content());
    }

    @Test
    void chat_forwardsUserContentToLlm() {
        when(ai.withDefaultLlm()).thenReturn(promptRunner);
        when(promptRunner.withId(anyString())).thenReturn(promptRunner);
        when(promptRunner.generateText(anyString())).thenReturn("答案是 42");

        ChatReply reply = new ChatAgent().chat(new UserInput("生命、宇宙以及一切的答案？"), ai);

        assertEquals("答案是 42", reply.content());
    }
}
