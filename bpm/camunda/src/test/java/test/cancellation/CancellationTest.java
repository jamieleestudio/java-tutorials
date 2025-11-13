package test.cancellation;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;

import java.util.List;

@Slf4j
public class CancellationTest extends CamundaBusinessProcessConfigurationTest {


    @Test
    public void deleteProcessInstance() {
        var processInstanceIds = List.of("15804","15904","16701");
        for (String processInstanceId : processInstanceIds) {
            extension.getRuntimeService().deleteProcessInstance(processInstanceId, "delete process instance");
            log.info(">>>>>>>>>>>>>>>>>>process instance id:{}",processInstanceId);
        }
    }


    @Test
    public void cancellationInstance() {
        ProcessInstance processInstance = super.deploymentAndStart("activity_instance_cancellation", "bpmn/cancellation/activity_instance_cancellation.bpmn");
        String processInstanceId = processInstance.getProcessInstanceId();
        extension.getRuntimeService().createProcessInstanceModification(processInstanceId).cancelActivityInstance(processInstanceId).execute();
        log.info(">>>>>>>>>>>>>>>>>>process instance id:{}",processInstance.getProcessInstanceId());
    }


    @Test
    public void cancellationCop01() {
        var processInstance = extension.getRuntimeService().startProcessInstanceByKey("cop_01");
        log.info(">>>>>>>>>>>>>>>>>>process instance id:{}",processInstance.getProcessInstanceId());
    }

}
