package test.inclusive;

import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;

import java.util.HashMap;
import java.util.Map;

public class InclusiveTest extends CamundaBusinessProcessConfigurationTest {


    @Test
    public void deploy() {
        super.deployment("inclusive_03","bpmn/inclusive_03.bpmn");
    }

    @Test
    public void complete(){
        extension.getRuntimeService().deleteProcessInstance("5901","del");
    }


    @Test
    public void delete(){
//        extension.getRuntimeService().deleteProcessInstance("3301","del");
//        extension.getRuntimeService().createProcessInstanceModification("3401").startBeforeActivity("G_1_B").execute();

        complete("6201","A_11");

    }


    @Test
    public void start(){
//        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_01");
//        System.out.println(processInstance.getProcessInstanceId());

        Map<String,Object> variables = new HashMap<>();
        variables.put("a",true);
        variables.put("b",true);
        variables.put("c",true);
        extension.getTaskService().complete("104",variables);
    }


    @Test
    public void test() {
        super.deployment("one_map", "bpmn/cop_04.bpmn");
    }

    @Test
    public void in_03(){
        var processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_03");
        var processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_2");
        setVariable(processInstanceId,"G_2_1",true);
        setVariable(processInstanceId,"G_2_2",false);
        setVariable(processInstanceId,"G_2_3",true);
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_5");
        cancelExecution(processInstanceId, Lists.newArrayList("A_5"),"A_3");
        contains(processInstanceId,"A_3");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_5");
        complete(processInstanceId,"A_5");

        //extension.getRuntimeService().deleteProcessInstance(processInstanceId,"del");
    }



    @Test
    public void in_pa_01(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_pa_01");
        String processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        setVariable(processInstanceId,"a",true);
        setVariable(processInstanceId,"b",true);
        setVariable(processInstanceId,"c",true);
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_3","A_4","A_5","A_6");

        complete(processInstanceId,"A_3","A_4");
        contains(processInstanceId,"A_2","A_5","A_6");
        complete(processInstanceId,"A_5","A_6");
        contains(processInstanceId,"A_2","A_9");

        setVariable(processInstanceId,"h",true);
        setVariable(processInstanceId,"i",false);

        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_7","A_9");
        complete(processInstanceId,"A_7");
        contains(processInstanceId,"A_10","A_9");
        complete(processInstanceId,"A_10","A_9");
        contains(processInstanceId,"A_11");
        complete(processInstanceId,"A_11");

    }

    @Test
    public void in_ex_01(){

        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_ex_01");
        String processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        setVariable(processInstanceId,"f",true);

        setVariable(processInstanceId,"a",true);
        setVariable(processInstanceId,"b",true);
        setVariable(processInstanceId,"c",false);
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_4","A_3");

        complete(processInstanceId,"A_4","A_3");
        contains(processInstanceId,"A_9");
        complete(processInstanceId,"A_9");
        contains(processInstanceId,"A_11");
        complete(processInstanceId,"A_11");


        processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_ex_01");
        processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        setVariable(processInstanceId,"f",true);

        setVariable(processInstanceId,"a",true);
        setVariable(processInstanceId,"b",true);
        setVariable(processInstanceId,"c",true);

        setVariable(processInstanceId,"d",true);
        setVariable(processInstanceId,"e",false);

        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_4","A_3","A_5");
        complete(processInstanceId,"A_4","A_3");
        contains(processInstanceId,"A_5");
        complete(processInstanceId,"A_5");
        contains(processInstanceId,"A_9");
        complete(processInstanceId,"A_9");
        contains(processInstanceId,"A_11");
        complete(processInstanceId,"A_11");

    }

    @Test
    public void in_02(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_02");
        String processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");

        setVariable(processInstanceId,"a",true);
        setVariable(processInstanceId,"b",true);
        setVariable(processInstanceId,"c",false);
        complete(processInstanceId,"A_1");

        contains(processInstanceId,"A_2","A_3");
        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"test");


        processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_02");
        processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");

        setVariable(processInstanceId,"a",true);
        setVariable(processInstanceId,"b",false);
        setVariable(processInstanceId,"c",false);
        complete(processInstanceId,"A_1");

        contains(processInstanceId,"A_2");

        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_5");
        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"test");


        processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_02");
        processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");

        setVariable(processInstanceId,"a",false);
        setVariable(processInstanceId,"b",true);
        setVariable(processInstanceId,"c",false);
        complete(processInstanceId,"A_1");

        contains(processInstanceId,"A_3");

        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_5");
        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"test");


        processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_02");
        processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");

        setVariable(processInstanceId,"a",false);
        setVariable(processInstanceId,"b",false);
        setVariable(processInstanceId,"c",true);
        complete(processInstanceId,"A_1");

        contains(processInstanceId,"A_5");

        complete(processInstanceId,"A_5");
    }


    @Test
    public void in_01(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_01");
        String processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");

        setVariable(processInstanceId,"a",true);
        setVariable(processInstanceId,"b",true);
        setVariable(processInstanceId,"c",false);
        complete(processInstanceId,"A_1");

        contains(processInstanceId,"A_2","A_3");
        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"test");


        processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_01");
        processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");

        setVariable(processInstanceId,"a",true);
        setVariable(processInstanceId,"b",false);
        setVariable(processInstanceId,"c",false);
        complete(processInstanceId,"A_1");

        contains(processInstanceId,"A_2");

        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_5");
        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"test");


        processInstance = extension.getRuntimeService().startProcessInstanceByKey("inclusive_01");
        processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");

        setVariable(processInstanceId,"a",true);
        setVariable(processInstanceId,"b",true);
        setVariable(processInstanceId,"c",true);
        complete(processInstanceId,"A_1");

        contains(processInstanceId,"A_2","A_3","A_4");
        complete(processInstanceId,"A_2");

        contains(processInstanceId,"A_3","A_4");
        complete(processInstanceId,"A_3","A_4");

        contains(processInstanceId,"A_5");
        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"test");
    }



}
