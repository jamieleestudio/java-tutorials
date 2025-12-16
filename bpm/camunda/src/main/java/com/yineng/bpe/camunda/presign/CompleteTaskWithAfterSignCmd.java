package com.yineng.bpe.camunda.presign;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;

import java.util.List;
import java.util.Map;

public class CompleteTaskWithAfterSignCmd implements Command<Void> {

    private final String taskId;
    private final Map<String, Object> variables;

    public CompleteTaskWithAfterSignCmd(String taskId, Map<String, Object> variables) {
        this.taskId = taskId;
        this.variables = variables;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        TaskEntity task = commandContext.getTaskManager().findTaskById(taskId);
        if (task == null) {
            throw new ProcessEngineException("task not found: " + taskId);
        }
        List<Task> subTasks = commandContext.getTaskManager().findTasksByParentTaskId(taskId);
        Object modeObj = task.getVariableLocal("afterSignMode");
        Object totalObj = task.getVariableLocal("afterSignTotal");
        Object requiredObj = task.getVariableLocal("afterSignRequired");
        if (modeObj == null || totalObj == null || requiredObj == null) {
            if (!subTasks.isEmpty()) {
                throw new ProcessEngineException("after-sign task is not completed");
            }
        } else {
            String modeName = String.valueOf(modeObj);
            int total = ((Number) totalObj).intValue();
            int required = ((Number) requiredObj).intValue();
            int open = subTasks.size();
            int finished = total - open;
            AddSignService.BeforeSignMode mode;
            try {
                mode = AddSignService.BeforeSignMode.valueOf(modeName);
            } catch (IllegalArgumentException ex) {
                mode = AddSignService.BeforeSignMode.ALL;
            }
            boolean pass;
            if (mode == AddSignService.BeforeSignMode.ALL) {
                pass = open == 0;
            } else if (mode == AddSignService.BeforeSignMode.ANY) {
                pass = finished >= 1;
            } else {
                pass = finished >= required;
            }
            if (!pass) {
                throw new ProcessEngineException("after-sign condition not satisfied");
            }
        }
        if (variables != null && !variables.isEmpty()) {
            task.setVariables(variables);
        }
        task.complete();
        return null;
    }
}

