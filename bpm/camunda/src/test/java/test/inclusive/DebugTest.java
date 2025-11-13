package test.inclusive;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;

public class DebugTest extends CamundaBusinessProcessConfigurationTest {

    @Test
    public void deploy() {
//        super.deployment("pa_simple","bpmn/pa_simple.bpmn");
//        super.deployment("in_simple","bpmn/in_simple.bpmn");
        extension.getRuntimeService().deleteProcessInstance("11601","delete");
        extension.getRuntimeService().deleteProcessInstance("11701","delete");
        extension.getRuntimeService().deleteProcessInstance("11801","delete");
        extension.getRuntimeService().deleteProcessInstance("11901","delete");
        extension.getRuntimeService().deleteProcessInstance("12001","delete");
        extension.getRuntimeService().deleteProcessInstance("12101","delete");
        extension.getRuntimeService().deleteProcessInstance("12201","delete");
        extension.getRuntimeService().deleteProcessInstance("12301","delete");
        extension.getRuntimeService().deleteProcessInstance("12401","delete");
        extension.getRuntimeService().deleteProcessInstance("12501","delete");
    }

    @Test
    public void debugPa(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_simple");
//        complete(processInstance.getProcessInstanceId(),"A_1");
//        complete(processInstance.getProcessInstanceId(),"A_2");
//        extension.getRuntimeService().deleteProcessInstance("1701","delete");

//        complete(processInstance.getProcessInstanceId(),"A_1");
//        complete(processInstance.getProcessInstanceId(),"A_2","A_3");
//
//        cancelExecution(processInstance.getProcessInstanceId(), Lists.newArrayList("A_4"),"A_2");


//
//        setVariable("1901","G_1_1",true);
//        setVariable("1901","G_1_2",true);
        complete("2901","A_2");
    }

}
