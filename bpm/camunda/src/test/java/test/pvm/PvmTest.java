package test.pvm;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;

@Slf4j
public class PvmTest extends CamundaBusinessProcessConfigurationTest {

    @Test
    public void deployTest() {
        var deployment =  super.deployment("leave","bpmn/pvm/leave.bpmn");
        extension.getRepositoryService().deleteDeployment(deployment.getId());
    }

    @Test
    public void startProcess() {
        var deployment =  super.deployment("leave","bpmn/pvm/leave.bpmn");
        var processInstance = extension.getRuntimeService().startProcessInstanceByKey("leave");
        extension.getRuntimeService().deleteProcessInstance(processInstance.getId(),"delete");
        extension.getRepositoryService().deleteDeployment(deployment.getId());
    }

}
