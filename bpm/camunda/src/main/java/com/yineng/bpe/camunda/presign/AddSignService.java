package com.yineng.bpe.camunda.presign;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.task.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddSignService {

    private final CommandExecutor commandExecutor;
    private final TaskService taskService;

    private static final String VAR_BEFORE_MODE = "beforeSignMode";
    private static final String VAR_BEFORE_TOTAL = "beforeSignTotal";
    private static final String VAR_BEFORE_REQUIRED = "beforeSignRequired";
    private static final String VAR_AFTER_MODE = "afterSignMode";
    private static final String VAR_AFTER_TOTAL = "afterSignTotal";
    private static final String VAR_AFTER_REQUIRED = "afterSignRequired";
    private static final String VAR_SIGN_TYPE = "addSignType";

    public AddSignService(ProcessEngine processEngine) {
        this.commandExecutor = ((ProcessEngineImpl) processEngine)
                .getProcessEngineConfiguration()
                .getCommandExecutorTxRequired();
        this.taskService = processEngine.getTaskService();
    }

    public String addBeforeSignTask(String taskId, String assignee) {
        String id = commandExecutor.execute(new BeforeSignCmd(taskId, assignee));
        taskService.setVariableLocal(taskId, VAR_BEFORE_MODE, SignMode.ALL.name());
        taskService.setVariableLocal(taskId, VAR_BEFORE_TOTAL, 1);
        taskService.setVariableLocal(taskId, VAR_BEFORE_REQUIRED, 1);
        taskService.setVariableLocal(taskId, VAR_SIGN_TYPE, SignType.BEFORE.name());
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
            taskService.setVariableLocal(taskId, VAR_BEFORE_MODE, SignMode.ALL.name());
            taskService.setVariableLocal(taskId, VAR_BEFORE_TOTAL, total);
            taskService.setVariableLocal(taskId, VAR_BEFORE_REQUIRED, total);
            taskService.setVariableLocal(taskId, VAR_SIGN_TYPE, SignType.BEFORE.name());
        }
        return result;
    }

    public List<String> addBeforeSignTasks(String taskId, Collection<String> assignees, SignMode mode, Integer required) {
        List<String> ids = addBeforeSignTasks(taskId, assignees);
        int total = ids.size();
        if (total == 0) {
            return ids;
        }
        SignMode useMode = mode == null ? SignMode.ALL : mode;
        int need;
        if (useMode == SignMode.ALL) {
            need = total;
        } else if (useMode == SignMode.ANY) {
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
        taskService.setVariableLocal(taskId, VAR_BEFORE_MODE, useMode.name());
        taskService.setVariableLocal(taskId, VAR_BEFORE_TOTAL, total);
        taskService.setVariableLocal(taskId, VAR_BEFORE_REQUIRED, need);
        taskService.setVariableLocal(taskId, VAR_SIGN_TYPE, SignType.BEFORE.name());
        return ids;
    }

    public void completeWithBeforeSign(String taskId, Map<String, Object> variables) {
        commandExecutor.execute(new CompleteTaskWithBeforeSignCmd(taskId, variables));
    }

    public String addAfterSignTask(String taskId, String assignee) {
        String id = commandExecutor.execute(new AfterSignCmd(taskId, assignee));
        taskService.setVariableLocal(taskId, VAR_AFTER_MODE, SignMode.ALL.name());
        taskService.setVariableLocal(taskId, VAR_AFTER_TOTAL, 1);
        taskService.setVariableLocal(taskId, VAR_AFTER_REQUIRED, 1);
        taskService.setVariableLocal(taskId, VAR_SIGN_TYPE, SignType.AFTER.name());
        return id;
    }

    public List<String> addAfterSignTasks(String taskId, Collection<String> assignees) {
        List<String> result = new ArrayList<>();
        if (assignees == null) {
            return result;
        }
        for (String assignee : assignees) {
            if (assignee == null || assignee.isEmpty()) {
                continue;
            }
            String id = commandExecutor.execute(new AfterSignCmd(taskId, assignee));
            result.add(id);
        }
        int total = result.size();
        if (total > 0) {
            taskService.setVariableLocal(taskId, VAR_AFTER_MODE, SignMode.ALL.name());
            taskService.setVariableLocal(taskId, VAR_AFTER_TOTAL, total);
            taskService.setVariableLocal(taskId, VAR_AFTER_REQUIRED, total);
            taskService.setVariableLocal(taskId, VAR_SIGN_TYPE, SignType.AFTER.name());
        }
        return result;
    }

    public List<String> addAfterSignTasks(String taskId, Collection<String> assignees, SignMode mode, Integer required) {
        List<String> ids = addAfterSignTasks(taskId, assignees);
        int total = ids.size();
        if (total == 0) {
            return ids;
        }
        SignMode useMode = mode == null ? SignMode.ALL : mode;
        int need;
        if (useMode == SignMode.ALL) {
            need = total;
        } else if (useMode == SignMode.ANY) {
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
        taskService.setVariableLocal(taskId, VAR_AFTER_MODE, useMode.name());
        taskService.setVariableLocal(taskId, VAR_AFTER_TOTAL, total);
        taskService.setVariableLocal(taskId, VAR_AFTER_REQUIRED, need);
        taskService.setVariableLocal(taskId, VAR_SIGN_TYPE, SignType.AFTER.name());
        return ids;
    }

    public void completeWithAfterSign(String taskId, Map<String, Object> variables) {
        commandExecutor.execute(new CompleteTaskWithAfterSignCmd(taskId, variables));
    }

    public void completeTask(String taskId, Map<String, Object> variables) {
        Object type = taskService.getVariableLocal(taskId, VAR_SIGN_TYPE);
        if (type != null) {
            String name = String.valueOf(type);
            if (SignType.BEFORE.name().equals(name)) {
                completeWithBeforeSign(taskId, variables);
                return;
            }
            if (SignType.AFTER.name().equals(name)) {
                completeWithAfterSign(taskId, variables);
                return;
            }
        }
        if (variables == null || variables.isEmpty()) {
            taskService.complete(taskId);
        } else {
            taskService.complete(taskId, variables);
        }
    }

    public List<String> addCosigners(String taskId, Collection<String> assignees) {
        return addAfterSignTasks(taskId, assignees, SignMode.ALL, null);
    }

    public List<String> addCosigners(String taskId, Collection<String> assignees, SignMode mode, Integer required) {
        return addAfterSignTasks(taskId, assignees, mode, required);
    }

    public void removeCosigners(String taskId, Collection<String> assignees) {
        if (assignees == null) {
            return;
        }
        Set<String> removeAssignees = new HashSet<>(assignees);
        if (removeAssignees.isEmpty()) {
            return;
        }
        List<Task> subTasks = taskService.createTaskQuery().taskParentTaskId(taskId).list();
        int removed = 0;
        for (Task t : subTasks) {
            String a = t.getAssignee();
            if (a != null && removeAssignees.contains(a)) {
                taskService.deleteTask(t.getId(), true);
                removed++;
            }
        }
        if (removed == 0) {
            return;
        }
        Object totalObj = taskService.getVariableLocal(taskId, VAR_AFTER_TOTAL);
        Object requiredObj = taskService.getVariableLocal(taskId, VAR_AFTER_REQUIRED);
        Object modeObj = taskService.getVariableLocal(taskId, VAR_AFTER_MODE);
        if (totalObj == null || modeObj == null || requiredObj == null) {
            return;
        }
        int total = ((Number) totalObj).intValue() - removed;
        if (total <= 0) {
            taskService.removeVariableLocal(taskId, VAR_AFTER_MODE);
            taskService.removeVariableLocal(taskId, VAR_AFTER_TOTAL);
            taskService.removeVariableLocal(taskId, VAR_AFTER_REQUIRED);
            taskService.removeVariableLocal(taskId, VAR_SIGN_TYPE);
            return;
        }
        int required = ((Number) requiredObj).intValue();
        if (required > total) {
            required = total;
        }
        taskService.setVariableLocal(taskId, VAR_AFTER_TOTAL, total);
        taskService.setVariableLocal(taskId, VAR_AFTER_REQUIRED, required);
    }
}
