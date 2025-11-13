package test.variable;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;

@Slf4j
public class VariableTest extends CamundaBusinessProcessConfigurationTest {


    @Test
    public void startBeforeActivity() {
        ProcessInstance processInstance = super.deploymentAndStart("activity_instance_cancellation", "bpmn/cancellation/activity_instance_cancellation.bpmn");
        String processInstanceId = processInstance.getProcessInstanceId();
        extension.getRuntimeService().setVariable(processInstanceId,"testVariable","你好");
        log.info(">>>>>>>>>>>>>>>>>>process instance id:{}",processInstance.getProcessInstanceId());
    }


}
