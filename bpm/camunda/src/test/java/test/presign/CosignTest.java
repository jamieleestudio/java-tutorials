package test.presign;

import com.yineng.bpe.camunda.presign.AddSignService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;

import java.util.Arrays;
import java.util.List;

public class CosignTest extends CamundaBusinessProcessConfigurationTest {

    @Test
    public void cosign_all_must_complete() {
        var processInstance = deploymentAndStart("pa_simple", "bpmn/pa_simple.bpmn");
        var processInstanceId = processInstance.getProcessInstanceId();
        Task task = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).singleResult();
        Assertions.assertNotNull(task);

        AddSignService service = new AddSignService(extension.getProcessEngine());
        List<String> users = Arrays.asList("c1", "c2");
        service.addCosigners(task.getId(), users);

        List<Task> subTasks = extension.getTaskService().createTaskQuery()
                .taskParentTaskId(task.getId())
                .list();
        Assertions.assertEquals(2, subTasks.size());

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

    @Test
    public void cosign_remove_one_then_complete() {
        var processInstance = deploymentAndStart("pa_simple", "bpmn/pa_simple.bpmn");
        var processInstanceId = processInstance.getProcessInstanceId();
        Task task = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).singleResult();
        Assertions.assertNotNull(task);

        AddSignService service = new AddSignService(extension.getProcessEngine());
        List<String> users = Arrays.asList("c1", "c2", "c3");
        service.addCosigners(task.getId(), users);

        List<Task> subTasks = extension.getTaskService().createTaskQuery()
                .taskParentTaskId(task.getId())
                .list();
        Assertions.assertEquals(3, subTasks.size());

        service.removeCosigners(task.getId(), List.of("c3"));

        List<Task> subTasksAfterRemove = extension.getTaskService().createTaskQuery()
                .taskParentTaskId(task.getId())
                .list();
        Assertions.assertEquals(2, subTasksAfterRemove.size());

        Assertions.assertThrows(ProcessEngineException.class,
                () -> service.completeWithAfterSign(task.getId(), null));

        extension.getTaskService().complete(subTasksAfterRemove.get(0).getId());
        Assertions.assertThrows(ProcessEngineException.class,
                () -> service.completeWithAfterSign(task.getId(), null));

        extension.getTaskService().complete(subTasksAfterRemove.get(1).getId());
        service.completeWithAfterSign(task.getId(), null);

        var nextTasks = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
        Assertions.assertFalse(nextTasks.isEmpty());
    }
}

