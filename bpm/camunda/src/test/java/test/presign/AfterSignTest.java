package test.presign;

import com.yineng.bpe.camunda.presign.AddSignService;
import com.yineng.bpe.camunda.presign.SignMode;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AfterSignTest extends CamundaBusinessProcessConfigurationTest {

    @Test
    public void afterSign_single_user() {
        var processInstance = deploymentAndStart("pa_simple", "bpmn/pa_simple.bpmn");
        var processInstanceId = processInstance.getProcessInstanceId();
        Task task = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).singleResult();
        Assertions.assertNotNull(task);

        AddSignService service = new AddSignService(extension.getProcessEngine());
        String afterTaskId = service.addAfterSignTask(task.getId(), "after_user");
        Assertions.assertNotNull(afterTaskId);

        Task afterTask = extension.getTaskService().createTaskQuery().taskId(afterTaskId).singleResult();
        Assertions.assertNotNull(afterTask);
        Assertions.assertEquals(task.getId(), afterTask.getParentTaskId());

        Assertions.assertThrows(ProcessEngineException.class,
                () -> service.completeWithAfterSign(task.getId(), null));

        extension.getTaskService().complete(afterTaskId);
        var variables = new HashMap<String, Object>();
        variables.put("y", 1);
        service.completeWithAfterSign(task.getId(), variables);

        var nextTasks = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
        Assertions.assertFalse(nextTasks.isEmpty());
    }

    @Test
    public void afterSign_multi_user_at_least_two() {
        var processInstance = deploymentAndStart("pa_simple", "bpmn/pa_simple.bpmn");
        var processInstanceId = processInstance.getProcessInstanceId();
        Task task = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).singleResult();
        Assertions.assertNotNull(task);

        AddSignService service = new AddSignService(extension.getProcessEngine());
        List<String> users = Arrays.asList("d1", "d2", "d3");
        List<String> afterTaskIds = service.addAfterSignTasks(task.getId(), users, SignMode.AT_LEAST, 2);
        Assertions.assertEquals(users.size(), afterTaskIds.size());

        List<Task> subTasks = extension.getTaskService().createTaskQuery()
                .taskParentTaskId(task.getId())
                .list();
        Assertions.assertEquals(users.size(), subTasks.size());

        Assertions.assertThrows(ProcessEngineException.class,
                () -> service.completeWithAfterSign(task.getId(), null));

        extension.getTaskService().complete(subTasks.get(0).getId());
        Assertions.assertThrows(ProcessEngineException.class,
                () -> service.completeWithAfterSign(task.getId(), null));

        extension.getTaskService().complete(subTasks.get(1).getId());
        service.completeWithAfterSign(task.getId(), null);

        var nextTasks = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
        Assertions.assertFalse(nextTasks.isEmpty());
    }
}

