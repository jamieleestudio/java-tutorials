package com.yineng.bpe.camunda.presign;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BeforeSignService {

    private final CommandExecutor commandExecutor;
    private final TaskService taskService;

    public BeforeSignService(ProcessEngine processEngine) {
        this.commandExecutor = ((ProcessEngineImpl) processEngine)
                .getProcessEngineConfiguration()
                .getCommandExecutorTxRequired();
        this.taskService = processEngine.getTaskService();
    }

    public String addBeforeSignTask(String taskId, String assignee) {
        String id = commandExecutor.execute(new BeforeSignCmd(taskId, assignee));
        taskService.setVariableLocal(taskId, "beforeSignMode", BeforeSignMode.ALL.name());
        taskService.setVariableLocal(taskId, "beforeSignTotal", 1);
        taskService.setVariableLocal(taskId, "beforeSignRequired", 1);
        return id;
    }

    public List<String> addBeforeSignTasks(String taskId, Collection<String> assignees) {
        List<String> result = new ArrayList<>();
        if (assignees == null) {
            return result;
        }
        for (String assignee : assignees) {
            if (assignee == null || assignee.isEmpty()) {
                continue;
            }
            String id = commandExecutor.execute(new BeforeSignCmd(taskId, assignee));
            result.add(id);
        }
        int total = result.size();
        if (total > 0) {
            taskService.setVariableLocal(taskId, "beforeSignMode", BeforeSignMode.ALL.name());
            taskService.setVariableLocal(taskId, "beforeSignTotal", total);
            taskService.setVariableLocal(taskId, "beforeSignRequired", total);
        }
        return result;
    }

    public List<String> addBeforeSignTasks(String taskId, Collection<String> assignees, BeforeSignMode mode, Integer required) {
        List<String> ids = addBeforeSignTasks(taskId, assignees);
        int total = ids.size();
        if (total == 0) {
            return ids;
        }
        BeforeSignMode useMode = mode == null ? BeforeSignMode.ALL : mode;
        int need;
        if (useMode == BeforeSignMode.ALL) {
            need = total;
        } else if (useMode == BeforeSignMode.ANY) {
            need = 1;
        } else {
            int r = required == null ? total : required;
            if (r < 1) {
                r = 1;
            }
            if (r > total) {
                r = total;
            }
            need = r;
        }
        taskService.setVariableLocal(taskId, "beforeSignMode", useMode.name());
        taskService.setVariableLocal(taskId, "beforeSignTotal", total);
        taskService.setVariableLocal(taskId, "beforeSignRequired", need);
        return ids;
    }

    public void completeWithBeforeSign(String taskId, Map<String, Object> variables) {
        commandExecutor.execute(new CompleteTaskWithBeforeSignCmd(taskId, variables));
    }

    public enum BeforeSignMode {
        ALL,
        ANY,
        AT_LEAST
    }
}
