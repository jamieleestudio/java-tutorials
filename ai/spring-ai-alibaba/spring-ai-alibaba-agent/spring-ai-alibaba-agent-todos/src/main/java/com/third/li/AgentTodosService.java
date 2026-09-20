package com.third.li;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.interceptor.todolist.TodoListInterceptor;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/**
 * 任务清单：WriteTodosTool + TodoListInterceptor。
 *
 * <p>{@link TodoListInterceptor} 是 ModelInterceptor：它给 Agent 注入一个
 * {@code write_todos} 工具（内部是 {@code WriteTodosTool}），让模型在执行多步任务时
 * 自己维护"待办清单"（pending / in_progress / completed），
 * 并在每次模型调用前把当前清单注入上下文 —— 与 Claude Code 的 TodoList 同一范式。
 */
@Service
public class AgentTodosService {

    private final ReactAgent agent;

    public AgentTodosService(ChatModel chatModel) throws GraphStateException {
        this.agent = ReactAgent.builder()
                .name("todos-agent")
                .description("带任务清单的规划助手")
                .systemPrompt("""
                        你是一个多步任务执行者。接到任务后先用 write_todos 建立清单（3 步以内），\
                        每完成一步就更新对应条目为 completed，全部完成后给出总结。\
                        这里是演示环境，步骤内容可以虚构合理细节。""")
                .model(chatModel)
                .interceptors(TodoListInterceptor.builder().build())
                .build();
    }

    public String ask(String message) throws Exception {
        return agent.call(message).getText();
    }
}
