package com.yineng.bpe.camunda.transfer;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.task.Task;

public class TransferTaskService {

    private final CommandExecutor commandExecutor;
    private final TaskService taskService;

    public TransferTaskService(ProcessEngine processEngine) {
        this.commandExecutor = ((ProcessEngineImpl) processEngine)
                .getProcessEngineConfiguration()
                .getCommandExecutorTxRequired();
        this.taskService = processEngine.getTaskService();
    }

    public void transfer(String taskId, String fromUser, String toUser, String reason) {
        commandExecutor.execute(new TransferTaskCmd(taskId, fromUser, toUser));
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null && reason != null && !reason.isEmpty()) {
            taskService.createComment(
                    taskId,
                    task.getProcessInstanceId(),
                    "task transferred from " + fromUser + " to " + toUser + " reason: " + reason
            );
        }
    }
}

