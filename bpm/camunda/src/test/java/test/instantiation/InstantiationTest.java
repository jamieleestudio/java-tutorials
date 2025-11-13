package test.instantiation;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;

@Slf4j
public class InstantiationTest extends CamundaBusinessProcessConfigurationTest {

    @Test
    public void startBeforeActivity() {
        ProcessInstance processInstance = super.deploymentAndStart("activity_instance_cancellation", "bpmn/cancellation/activity_instance_cancellation.bpmn");
        String processInstanceId = processInstance.getProcessInstanceId();
        extension.getRuntimeService().createProcessInstanceModification(processInstanceId).cancelActivityInstance(processInstanceId)
                .startBeforeActivity("Activity_07mr4yh").execute();
        log.info(">>>>>>>>>>>>>>>>>>process instance id:{}",processInstance.getProcessInstanceId());
    }

    @Test
    public void customerStartBeforeActivity() {
        ProcessInstance processInstance = super.deploymentAndStart("cop_01", "bpmn\\cop_01.bpmn");
        String processInstanceId = processInstance.getProcessInstanceId();
        extension.getRuntimeService().createProcessInstanceModification(processInstanceId)
                .cancelActivityInstance(processInstanceId)
                .startBeforeActivity("A_3").execute();
        log.info(">>>>>>>>>>>>>>>>>>process instance id:{}",processInstance.getProcessInstanceId());
    }

    @Test
    public void deployment1() {
        super.deployment("cop_01", "bpmn\\cop_01.bpmn");
    }

    @Test
    public void deleteProcessInstance(){
          extension.getRuntimeService().deleteProcessInstance("6101","delete process instance");
    }

    @Test
    public void statProcessInstance() {
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("cop_01");
        log.info(">>>>>>>>>>>>>>>>>>process instance id:{}",processInstance.getProcessInstanceId());
    }

    @Test
    public void completeTask() {
        TaskService taskService = extension.getTaskService();
        taskService.setVariable("6505", "G_2", false);
//        taskService.complete("3506");
        taskService.complete("6505");
    }

}
