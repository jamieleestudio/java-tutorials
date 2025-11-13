package test.revoke;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;


@Slf4j
public class RevokeTest extends CamundaBusinessProcessConfigurationTest {
    @Test
    public void deploy(                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           ) {
        super.deployment("pa_11","bpmn/pa_11.bpmn");
        super.deployment("ex_01","bpmn/ex_01.bpmn");
        super.deployment("cop_01","bpmn/cop_01.bpmn");
        super.deployment("cop_02","bpmn/cop_02.bpmn");
        super.deployment("cop_03","bpmn/cop_03.bpmn");
        super.deployment("cop_04","bpmn/cop_04.bpmn");
        super.deployment("cop_05","bpmn/cop_05.bpmn");
        super.deployment("pa_01","bpmn/pa_01.bpmn");
        super.deployment("pa_02","bpmn/pa_02.bpmn");
        super.deployment("pa_03","bpmn/pa_03.bpmn");
        super.deployment("pa_04","bpmn/pa_04.bpmn");
        super.deployment("pa_05","bpmn/pa_05.bpmn");
        super.deployment("pa_06","bpmn/pa_06.bpmn");
        super.deployment("pa_07","bpmn/pa_07.bpmn");
        super.deployment("pa_08","bpmn/pa_08.bpmn");
        super.deployment("pa_09","bpmn/pa_09.bpmn");
        super.deployment("pa_10","bpmn/pa_10.bpmn");
        super.deployment("pa_11","bpmn/pa_11.bpmn");
    }



    @Test
    public void pa_11(){
        var processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_11");
        var processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");

        contains(processInstanceId,"A_2");
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3","A_4");
        cancelExecution(processInstanceId, Lists.newArrayList("A_3","A_4"),"A_2");
        contains(processInstanceId,"A_2");
        complete(processInstanceId,"A_2");

        contains(processInstanceId,"A_3","A_4");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_4");
        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_5");
        cancelExecution(processInstanceId, Lists.newArrayList("A_5"),"A_3");
        contains(processInstanceId,"A_3");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_5");
        complete(processInstanceId,"A_5");

    }

    @Test
    public void cop_05(){
        var processInstance = extension.getRuntimeService().startProcessInstanceByKey("cop_05");
        var processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");

        setVariable(processInstanceId,"a",true);
        setVariable(processInstanceId,"b",true);
        setVariable(processInstanceId,"c",true);

        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_3","A_4","A_5","A_6","A_2");
        cancelExecution(processInstanceId, Lists.newArrayList("A_3","A_4","A_5","A_6","A_2"),"A_1");


        contains(processInstanceId,"A_1");

        setVariable(processInstanceId,"a",true);
        setVariable(processInstanceId,"b",true);
        setVariable(processInstanceId,"c",true);

        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_3","A_4","A_5","A_6","A_2");
        complete(processInstanceId,"A_3");

        contains(processInstanceId,"A_4","A_5","A_6","A_2");
        complete(processInstanceId,"A_5");
        contains(processInstanceId,"A_4","A_6","A_2");
        complete(processInstanceId,"A_4","A_6");
        contains(processInstanceId,"A_9","A_2");
        cancelExecution(processInstanceId, Lists.newArrayList("A_9"),"A_3");
        contains(processInstanceId,"A_3","A_2");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_9","A_2");
        cancelExecution(processInstanceId, Lists.newArrayList("A_9"),"A_5");
        contains(processInstanceId,"A_5","A_2");
        complete(processInstanceId,"A_5");
        contains(processInstanceId,"A_9","A_2");

        setVariable(processInstanceId,"h",true);
        setVariable(processInstanceId,"i",true);
        complete(processInstanceId,"A_2");

        contains(processInstanceId,"A_9","A_7","A_8");
        complete(processInstanceId,"A_7");
        contains(processInstanceId,"A_9","A_8");
        complete(processInstanceId,"A_8");
        contains(processInstanceId,"A_9","A_10");

        cancelExecution(processInstanceId, Lists.newArrayList("A_10"),"A_7");
        contains(processInstanceId,"A_9","A_7");
        complete(processInstanceId,"A_7");
        contains(processInstanceId,"A_9","A_10");
        complete(processInstanceId,"A_9");
        contains(processInstanceId,"A_10");
        complete(processInstanceId,"A_10");
        contains(processInstanceId,"A_11");
        cancelExecution(processInstanceId, Lists.newArrayList("A_11"),"A_9");
        contains(processInstanceId,"A_9");
        complete(processInstanceId,"A_9");
        contains(processInstanceId,"A_11");
        cancelExecution(processInstanceId, Lists.newArrayList("A_11"),"A_10");

        contains(processInstanceId,"A_10");
        complete(processInstanceId,"A_10");
        contains(processInstanceId,"A_11");
        complete(processInstanceId,"A_11");
    }


    @Test
    public void cop_04(){

        var processInstance = extension.getRuntimeService().startProcessInstanceByKey("cop_04");
        var processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        setVariable(processInstanceId,"G_3",1);
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_5");
        cancelExecution(processInstanceId, Lists.newArrayList("A_2","A_5"),"A_1");
        contains(processInstanceId,"A_1");
        setVariable(processInstanceId,"G_3",1);
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_5");


        setVariable(processInstanceId,"G_2_1",true);
        setVariable(processInstanceId,"G_2_2",false);
        setVariable(processInstanceId,"G_2_3",true);
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3","A_5");

        cancelExecution(processInstanceId, Lists.newArrayList("A_3"),"A_2");
        contains(processInstanceId,"A_2","A_5");

        setVariable(processInstanceId,"G_2_1",true);
        setVariable(processInstanceId,"G_2_2",false);
        setVariable(processInstanceId,"G_2_3",true);
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3","A_5");

        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_5");
        complete(processInstanceId,"A_5");
        contains(processInstanceId,"A_7","A_8");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7","A_8"),"A_5");

        contains(processInstanceId,"A_5");
        complete(processInstanceId,"A_5");
        contains(processInstanceId,"A_7","A_8");

        complete(processInstanceId,"A_7","A_8");
        contains(processInstanceId,"A_11");


        cancelExecution(processInstanceId, Lists.newArrayList("A_11"),"A_3");
        contains(processInstanceId,"A_3");
        complete(processInstanceId,"A_3");

        contains(processInstanceId,"A_11");
        complete(processInstanceId,"A_11");

    }


    @Test
    public void cop_03(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("cop_03");
        String processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2");
        setVariable(processInstanceId,"G_1",false);
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3","A_4");
        cancelExecution(processInstanceId, Lists.newArrayList("A_3","A_4"),"A_2");

        contains(processInstanceId,"A_2");
        setVariable(processInstanceId,"G_1",false);
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3","A_4");
        complete(processInstanceId,"A_3","A_4");
        contains(processInstanceId,"A_7");
        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_3");
        contains(processInstanceId,"A_3");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_7");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_4");
        contains(processInstanceId,"A_4");
        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_7");
        setVariable(processInstanceId,"G_3",1);
        complete(processInstanceId,"A_7");
        contains(processInstanceId,"A_8");

        cancelExecution(processInstanceId, Lists.newArrayList("A_8"),"A_7");
        contains(processInstanceId,"A_7");
        setVariable(processInstanceId,"G_3",2);
        complete(processInstanceId,"A_7");
        contains(processInstanceId,"A_9");
        cancelExecution(processInstanceId, Lists.newArrayList("A_9"),"A_7");

        contains(processInstanceId,"A_7");
        setVariable(processInstanceId,"G_3",3);
        complete(processInstanceId,"A_7");
        contains(processInstanceId,"A_12");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");

        processInstance = extension.getRuntimeService().startProcessInstanceByKey("cop_03");
        processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2");
        setVariable(processInstanceId,"G_1",true);
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_5");
        complete(processInstanceId,"A_5");
        contains(processInstanceId,"A_6");
        complete(processInstanceId,"A_6");
        contains(processInstanceId,"A_10","A_11");
        cancelExecution(processInstanceId, Lists.newArrayList("A_10","A_11"),"A_6");
        contains(processInstanceId,"A_6");
        complete(processInstanceId,"A_6");
        contains(processInstanceId,"A_10","A_11");
        complete(processInstanceId,"A_10","A_11");
        contains(processInstanceId,"A_12");
        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");

    }


    @Test
    public void cop_02(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("cop_02");
        String processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        setVariable(processInstanceId,"G_2",false);
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_3");
        cancelExecution(processInstanceId, Lists.newArrayList("A_2","A_3"),"A_1");
        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_3");
        cancelExecution(processInstanceId, Lists.newArrayList("A_2","A_3"),"A_1");
        contains(processInstanceId,"A_1");

        setVariable(processInstanceId,"G_2",true);
        complete(processInstanceId,"A_1");

        contains(processInstanceId,"A_2");

        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_4");

        cancelExecution(processInstanceId, Lists.newArrayList("A_4"),"A_2");
        contains(processInstanceId,"A_2");
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_4");

        cancelExecution(processInstanceId, Lists.newArrayList("A_4"),"A_3");
        contains(processInstanceId,"A_3");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_4");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");
    }

    @Test
    public void cop_01(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("cop_01");
        String processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        setVariable(processInstanceId,"G_2",false);
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_3");
        cancelExecution(processInstanceId, Lists.newArrayList("A_2","A_3"),"A_1");
        contains(processInstanceId,"A_1");
        setVariable(processInstanceId,"G_2",true);
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2");

        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_4");

        cancelExecution(processInstanceId, Lists.newArrayList("A_4"),"A_2");
        contains(processInstanceId,"A_2");
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_4");


        cancelExecution(processInstanceId, Lists.newArrayList("A_4"),"A_3");
        contains(processInstanceId,"A_3");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_4");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");
    }

    @Test
    public void pa_09(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_09");
        String processInstanceId = processInstance.getProcessInstanceId();

        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_4","A_9");

        cancelExecution(processInstanceId, Lists.newArrayList("A_2","A_4","A_9"),"A_1");

        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_4","A_9");

        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_2","A_9");

        complete(processInstanceId,"A_9");
        contains(processInstanceId,"A_2","A_10","A_11");

        cancelExecution(processInstanceId, Lists.newArrayList("A_10","A_11"),"A_9");

        contains(processInstanceId,"A_2","A_9");
        complete(processInstanceId,"A_9");
        contains(processInstanceId,"A_2","A_10","A_11");

        complete(processInstanceId,"A_10");
        contains(processInstanceId,"A_2","A_11");
        complete(processInstanceId,"A_11");

        contains(processInstanceId,"A_2","A_7");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_4");
        contains(processInstanceId,"A_2","A_4");

        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_2","A_7");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_10");
        contains(processInstanceId,"A_2","A_10");

        complete(processInstanceId,"A_10");
        contains(processInstanceId,"A_2","A_7");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_11");
        contains(processInstanceId,"A_2","A_11");

        complete(processInstanceId,"A_11");
        contains(processInstanceId,"A_2","A_7");

        complete(processInstanceId,"A_2");

        contains(processInstanceId,"A_3","A_6","A_7");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_5","A_6","A_7");
        complete(processInstanceId,"A_5","A_6");
        contains(processInstanceId,"A_7");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_4");
        contains(processInstanceId,"A_4");
        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_7");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_10");
        contains(processInstanceId,"A_10");
        complete(processInstanceId,"A_10");
        contains(processInstanceId,"A_7");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_11");
        contains(processInstanceId,"A_11");
        complete(processInstanceId,"A_11");
        contains(processInstanceId,"A_7");

        complete(processInstanceId,"A_7");
        contains(processInstanceId,"A_8");


        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");
    }

    @Test
    public void pa_08(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_08");
        String processInstanceId = processInstance.getProcessInstanceId();

        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2");

        cancelExecution(processInstanceId, Lists.newArrayList("A_2"),"A_1");

        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");
    }

    @Test
    public void pa_07(){

        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_07");
        String processInstanceId = processInstance.getProcessInstanceId();

        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");

        contains(processInstanceId,"A_2","A_4");
        cancelExecution(processInstanceId, Lists.newArrayList("A_2","A_4"),"A_1");
        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_4");

        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3","A_6","A_4");

        cancelExecution(processInstanceId, Lists.newArrayList("A_3","A_6"),"A_2");
        contains(processInstanceId,"A_2","A_4");
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3","A_6","A_4");

        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_5","A_6","A_4");

        cancelExecution(processInstanceId, Lists.newArrayList("A_5"),"A_3");
        contains(processInstanceId,"A_3","A_6","A_4");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_5","A_6","A_4");

        complete(processInstanceId,"A_5","A_6");
        contains(processInstanceId,"A_4");

        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_7");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_4");
        contains(processInstanceId,"A_4");
        complete(processInstanceId,"A_4");

        contains(processInstanceId,"A_7");
        complete(processInstanceId,"A_7");

        contains(processInstanceId,"A_8");
        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");
    }

    @Test
    public void pa_06(){

        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_06");
        String processInstanceId = processInstance.getProcessInstanceId();

        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_3");

        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_2","A_4","A_5");

        complete(processInstanceId,"A_4");

        contains(processInstanceId,"A_2","A_5");
        complete(processInstanceId,"A_5");
        contains(processInstanceId,"A_2");

        complete(processInstanceId,"A_2");

        contains(processInstanceId,"A_6");


        cancelExecution(processInstanceId, Lists.newArrayList("A_6"),"A_4");
        contains(processInstanceId,"A_4");

        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_6");

        cancelExecution(processInstanceId, Lists.newArrayList("A_6"),"A_5");

        contains(processInstanceId,"A_5");
        complete(processInstanceId,"A_5");
        contains(processInstanceId,"A_6");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");

    }

    @Test
    public void pa_05(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_05");
        String processInstanceId = processInstance.getProcessInstanceId();

        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_3");

        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_2","A_4","A_5");

        cancelExecution(processInstanceId, Lists.newArrayList("A_4","A_5"),"A_3");
        contains(processInstanceId,"A_2","A_3");

        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_2","A_4","A_5");

        complete(processInstanceId,"A_4","A_5");
        contains(processInstanceId,"A_2","A_7");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_4");
        contains(processInstanceId,"A_2","A_4");
        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_2","A_7");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_5");
        contains(processInstanceId,"A_2","A_5");
        complete(processInstanceId,"A_5");
        contains(processInstanceId,"A_2","A_7");

        complete(processInstanceId,"A_7");
        contains(processInstanceId,"A_2");
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_6");


        cancelExecution(processInstanceId, Lists.newArrayList("A_6"),"A_2");
        contains(processInstanceId,"A_2");
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_6");

        cancelExecution(processInstanceId, Lists.newArrayList("A_6"),"A_7");
        contains(processInstanceId,"A_7");
        complete(processInstanceId,"A_7");
        contains(processInstanceId,"A_6");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");


        processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_05");
        processInstanceId = processInstance.getProcessInstanceId();

        complete(processInstanceId,"A_1");
        complete(processInstanceId,"A_2","A_3");
        complete(processInstanceId,"A_4","A_5");
        contains(processInstanceId,"A_7");

        cancelExecution(processInstanceId, Lists.newArrayList("A_7"),"A_4");
        contains(processInstanceId,"A_4");
        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_7");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");

    }

    @Test
    public void pa_04(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_04");
        String processInstanceId = processInstance.getProcessInstanceId();

        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_4");

        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_2","A_5","A_6");

        cancelExecution(processInstanceId, Lists.newArrayList("A_5","A_6"),"A_4");

        contains(processInstanceId,"A_2","A_4");

        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_2","A_5","A_6");

        complete(processInstanceId,"A_5","A_6");
        contains(processInstanceId,"A_2");

        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");

    }

    @Test
    public void pa_03(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_03");
        String processInstanceId = processInstance.getProcessInstanceId();

        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2");

        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3");

        cancelExecution(processInstanceId, Lists.newArrayList("A_3"),"A_2");

        contains(processInstanceId,"A_2");
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");
    }



    @Test
    public void pa_02(){

        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_02");
        String processInstanceId = processInstance.getProcessInstanceId();

        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_4");

        cancelExecution(processInstanceId, Lists.newArrayList("A_2","A_4"),"A_1");
        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_4");

        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3","A_4");

        cancelExecution(processInstanceId, Lists.newArrayList("A_3"),"A_2");
        contains(processInstanceId,"A_2","A_4");

        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_2");

        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3");

        cancelExecution(processInstanceId, Lists.newArrayList("A_3"),"A_2");
        contains(processInstanceId,"A_2");

        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3");

        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_5");

        cancelExecution(processInstanceId, Lists.newArrayList("A_5"),"A_3");
        contains(processInstanceId,"A_3");


        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_5");

        cancelExecution(processInstanceId, Lists.newArrayList("A_5"),"A_4");
        contains(processInstanceId,"A_4");

        complete(processInstanceId,"A_4");
        contains(processInstanceId,"A_5");


        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");

        processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_02");
        processInstanceId = processInstance.getProcessInstanceId();

        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_4");
        cancelExecution(processInstanceId, Lists.newArrayList("A_2","A_4"),"A_1");
        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2","A_4");
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3","A_4");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_4");
        cancelExecution(processInstanceId, Lists.newArrayList(),"A_3");
        contains(processInstanceId,"A_3","A_4");
        complete(processInstanceId,"A_3","A_4");
        contains(processInstanceId,"A_5");
        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");

    }


    @Test
    public void pa_01(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_01");
        String processInstanceId = processInstance.getProcessInstanceId();

        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2");

        cancelExecution(processInstanceId, Lists.newArrayList("A_2"),"A_1");
        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_2");


        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3");

        cancelExecution(processInstanceId, Lists.newArrayList("A_3"),"A_2");

        contains(processInstanceId,"A_2");
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_3");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");
    }


    @Test
    public void ex_01(){

        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("ex_01");
        String processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        setVariable(processInstanceId,"G_1",true);
        complete(processInstanceId,"A_1");

        contains(processInstanceId,"A_3");
        setVariable(processInstanceId,"G_2",true);
        complete(processInstanceId,"A_3");

        contains(processInstanceId,"A_5");
        complete(processInstanceId,"A_5");

        contains(processInstanceId,"A_6");

        cancelExecution(processInstanceId, Lists.newArrayList("A_6"),"A_5");
        contains(processInstanceId,"A_5");
        complete(processInstanceId,"A_5");
        contains(processInstanceId,"A_6");

        cancelExecution(processInstanceId, Lists.newArrayList("A_6"),"A_2");
        contains(processInstanceId,"A_2");
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_6");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");

    }

    @Test
    public void pa_10(){
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_10");
        String processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_3","A_2","A_5");
        complete(processInstanceId,"A_3","A_2","A_5");
        contains(processInstanceId,"A_4");
        cancelExecution(processInstanceId, Lists.newArrayList("A_4"),"A_2");
        complete(processInstanceId,"A_2");
        contains(processInstanceId,"A_4");
        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");
        processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_10");
        processInstanceId = processInstance.getProcessInstanceId();
        contains(processInstanceId,"A_1");
        complete(processInstanceId,"A_1");
        contains(processInstanceId,"A_3","A_2","A_5");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_2","A_5");
        cancelExecution(processInstanceId, Lists.newArrayList(),"A_3");
        contains(processInstanceId,"A_3","A_2","A_5");
        complete(processInstanceId,"A_3");
        contains(processInstanceId,"A_2","A_5");

        //contains(processInstanceId,"A_4");

        extension.getRuntimeService().deleteProcessInstance(processInstanceId,"");

    }

}
