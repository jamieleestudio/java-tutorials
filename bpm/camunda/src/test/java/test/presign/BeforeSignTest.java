package test.presign;

import com.yineng.bpe.camunda.presign.BeforeSignService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BeforeSignTest extends CamundaBusinessProcessConfigurationTest {

    @Test
    public void beforeSign_for_user_task() {
        var processInstance = deploymentAndStart("pa_simple", "bpmn/pa_simple.bpmn");
        var processInstanceId = processInstance.getProcessInstanceId();
        Task task = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).singleResult();
        Assertions.assertNotNull(task);
        BeforeSignService service = new BeforeSignService(extension.getProcessEngine());
        String beforeTaskId = service.addBeforeSignTask(task.getId(), "before_user");
        Assertions.assertNotNull(beforeTaskId);
        Task beforeTask = extension.getTaskService().createTaskQuery().taskId(beforeTaskId).singleResult();
        Assertions.assertNotNull(beforeTask);
        Assertions.assertEquals(task.getId(), beforeTask.getParentTaskId());
        Assertions.assertEquals("before_user", beforeTask.getAssignee());
        extension.getTaskService().complete(beforeTaskId);
        var variables = new HashMap<String, Object>();
        variables.put("x", 1);
        service.completeWithBeforeSign(task.getId(), variables);
        var nextTasks = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
        Assertions.assertFalse(nextTasks.isEmpty());
    }

    @Test
    public void beforeSign_for_multi_user() {
        var processInstance = deploymentAndStart("pa_simple", "bpmn/pa_simple.bpmn");
        var processInstanceId = processInstance.getProcessInstanceId();
        Task task = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).singleResult();
        Assertions.assertNotNull(task);

        BeforeSignService service = new BeforeSignService(extension.getProcessEngine());
        List<String> users = Arrays.asList("u1", "u2", "u3");
        List<String> beforeTaskIds = service.addBeforeSignTasks(task.getId(), users, BeforeSignService.BeforeSignMode.AT_LEAST, 2);
        Assertions.assertEquals(users.size(), beforeTaskIds.size());

        List<Task> subTasks = extension.getTaskService().createTaskQuery()
                .taskParentTaskId(task.getId())
                .list();
        Assertions.assertEquals(users.size(), subTasks.size());

        Set<String> assignees = subTasks.stream().map(Task::getAssignee).collect(Collectors.toSet());
        Assertions.assertTrue(assignees.containsAll(users));

        Assertions.assertThrows(ProcessEngineException.class,
                () -> service.completeWithBeforeSign(task.getId(), null));

        extension.getTaskService().complete(subTasks.get(0).getId());
        Assertions.assertThrows(ProcessEngineException.class,
                () -> service.completeWithBeforeSign(task.getId(), null));

        extension.getTaskService().complete(subTasks.get(1).getId());
        service.completeWithBeforeSign(task.getId(), null);
        var nextTasks = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
        Assertions.assertFalse(nextTasks.isEmpty());
    }
}
