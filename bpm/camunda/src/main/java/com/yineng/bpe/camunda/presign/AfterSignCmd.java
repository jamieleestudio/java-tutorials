package com.yineng.bpe.camunda.presign;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

import java.util.Date;

public class AfterSignCmd implements Command<String> {

    private final String taskId;
    private final String assignee;

    public AfterSignCmd(String taskId, String assignee) {
        this.taskId = taskId;
        this.assignee = assignee;
    }

    @Override
    public String execute(CommandContext commandContext) {
        TaskEntity target = commandContext.getTaskManager().findTaskById(taskId);
        if (target == null) {
            throw new ProcessEngineException("task not found: " + taskId);
        }
        if (assignee == null || assignee.isEmpty()) {
            throw new ProcessEngineException("assignee is required");
        }
        TaskEntity subTask = new TaskEntity();
        subTask.setParentTaskId(target.getId());
        subTask.setProcessDefinitionId(target.getProcessDefinitionId());
        subTask.setProcessInstanceId(target.getProcessInstanceId());
        subTask.setTaskDefinitionKey(target.getTaskDefinitionKey());
        subTask.setName(target.getName());
        subTask.setAssignee(assignee);
        subTask.setOwner(target.getAssignee());
        subTask.setCreateTime(new Date());
        subTask.setPriority(target.getPriority());
        subTask.setDescription("after-sign");
        commandContext.getTaskManager().insertTask(subTask);
        return subTask.getId();
    }
}

