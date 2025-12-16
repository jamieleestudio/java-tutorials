package com.yineng.bpe.camunda.transfer;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.IdentityLinkType;

public class TransferTaskCmd implements Command<Void> {

    private final String taskId;
    private final String fromUser;
    private final String toUser;

    public TransferTaskCmd(String taskId, String fromUser, String toUser) {
        this.taskId = taskId;
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        TaskEntity task = commandContext.getTaskManager().findTaskById(taskId);
        if (task == null) {
            throw new ProcessEngineException("task not found: " + taskId);
        }
        if (toUser == null || toUser.isEmpty()) {
            throw new ProcessEngineException("new assignee is required");
        }
        String currentAssignee = task.getAssignee();
        if (currentAssignee != null) {
            if (fromUser != null && !fromUser.isEmpty() && !fromUser.equals(currentAssignee)) {
                throw new ProcessEngineException("only current assignee can transfer");
            }
            String owner = currentAssignee;
            task.setOwner(owner);
            task.setAssignee(toUser);
        } else {
            if (fromUser == null || fromUser.isEmpty()) {
                throw new ProcessEngineException("fromUser is required for candidate transfer");
            }
            boolean exists = false;
            for (var link : task.getIdentityLinks()) {
                if (IdentityLinkType.CANDIDATE.equals(link.getType()) && fromUser.equals(link.getUserId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                throw new ProcessEngineException("candidate user not found for transfer");
            }
            task.deleteUserIdentityLink(fromUser, IdentityLinkType.CANDIDATE);
            task.addUserIdentityLink(toUser, IdentityLinkType.CANDIDATE);
        }
        return null;
    }
}
