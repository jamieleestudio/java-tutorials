package test.transfer;

import com.yineng.bpe.camunda.transfer.TransferTaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;

public class TransferTaskTest extends CamundaBusinessProcessConfigurationTest {

    @Test
    public void transfer_task_changes_assignee_and_owner() {
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("transfer_process")
                .startEvent()
                .userTask()
                .camundaAssignee("original_assignee")
                .endEvent()
                .done();

        extension.getRepositoryService().createDeployment()
                .name("transfer_process")
                .source("test")
                .addModelInstance("transfer_process.bpmn", modelInstance)
                .deploy();

        var processInstance = extension.getRuntimeService().startProcessInstanceByKey("transfer_process");
        var processInstanceId = processInstance.getProcessInstanceId();
        Task task = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).singleResult();
        Assertions.assertNotNull(task);
        String originalAssignee = task.getAssignee();
        String newAssignee = "transfer_target";

        TransferTaskService service = new TransferTaskService(extension.getProcessEngine());
        service.transfer(task.getId(), originalAssignee, newAssignee, "transfer for test");

        Task updated = extension.getTaskService().createTaskQuery().taskId(task.getId()).singleResult();
        Assertions.assertNotNull(updated);
        Assertions.assertEquals(newAssignee, updated.getAssignee());
        Assertions.assertEquals(originalAssignee, updated.getOwner());
    }
}
