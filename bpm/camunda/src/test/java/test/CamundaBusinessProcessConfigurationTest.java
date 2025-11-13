package test;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import redis.clients.jedis.Jedis;
import test.revoke.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ExtendWith(ProcessEngineExtension.class)
public class CamundaBusinessProcessConfigurationTest {

     static ProcessEngine processEngine  =
            ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                    .setJdbcDriver("com.mysql.cj.jdbc.Driver")
                    .setJdbcUrl("jdbc:mysql://localhost:3306/ncp_practice?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true")
                    .setJdbcUsername("root")
                    .setJdbcPassword("123456")
                    .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                    .setHistory(ProcessEngineConfiguration.HISTORY_AUDIT)
                    .buildProcessEngine();

    @RegisterExtension
    protected static ProcessEngineExtension extension = ProcessEngineExtension.builder().useProcessEngine(processEngine).build();

    protected Deployment deployment(String key,String bpmnpath){
        var bpmnString = IoUtil.readClasspathResourceAsString(bpmnpath);
        bpmnString = bpmnString.replaceAll("pair=\"\\w\"","").replaceAll("position=\"\\w\"","");
        return extension.getRepositoryService().createDeployment()
                .name(key)
                .source("本地测试")
                .tenantId("TEST_TENANT")
                .addString(key+".bpmn",bpmnString)
                .deploy();
    }

    protected ProcessInstance deploymentAndStart(String key, String bpmnpath){
        deployment(key,bpmnpath);
        return extension.getRuntimeService().startProcessInstanceByKey(key);
    }


    protected void setVariable(String processInstanceId,String variableName,Object value){
        extension.getRuntimeService().setVariable(processInstanceId,variableName,value);
    }


    private String getProcessKey(String processInstanceId){
        HistoricProcessInstance historicProcessInstance =
                extension.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        return historicProcessInstance.getProcessDefinitionKey();
    }

    private void repair(String processInstanceId,Task task,int repairCount){

        if(repairCount >99){
            throw new RuntimeException();
        }


        List<Execution> executionList =  extension.getRuntimeService().createExecutionQuery().processInstanceId(processInstanceId).list();

        var nodeList = NodeTestUtil.buildProcessObject("/bpmn/"+this.getProcessKey(processInstanceId)+".bpmn");
        var nodeMap = nodeList.stream().collect(Collectors.toMap(Node::getNodeId, i->i));
        var taskDefinitionKey = task.getTaskDefinitionKey();

        var parent =
                DiagramHelper.INSTANCE.findParentParallelGateWayBackList(nodeList,task.getTaskDefinitionKey());
        var parentIdList = parent.stream().map(Node::getNodeId).collect(Collectors.toSet());

        if(CollectionUtils.isEmpty(parentIdList)){
            log.info("parent id is empty");
            return;
        }else{
            log.info("parent id {}",parentIdList);
        }

        var nextParallelGateWayNodeList = findNextParallelGateWayNodeList(nodeList,taskDefinitionKey);
        if(CollectionUtils.isEmpty(nextParallelGateWayNodeList)){
            log.info("next id is empty");
            return;
        }
        var firstNode = nextParallelGateWayNodeList.get(0);
        if(firstNode.getPairPosition() == PairPositionEnum.F){
            log.info("next id is f");
            return;
        }

        var parentParallelGateWayBack =  DiagramHelper.INSTANCE.findParentParallelGateWayBackList(nodeMap.values(),taskDefinitionKey);

        var parentParallelGateWayBackIdList = parentParallelGateWayBack.stream().map(Node::getNodeId).collect(Collectors.toSet());
        //获取下一并行网关节点
        var nextParallelGateWayIdList = nextParallelGateWayNodeList.stream().map(Node::getNodeId).toList();

        log.info("repair group >>>>>>>>>>");

        var needRepairGateWay = new HashSet<String>();

        for(Execution execution : executionList){
            ExecutionEntity executionEntity = (ExecutionEntity) execution;
            String activityId = executionEntity.getActivityId();
            if(StringUtils.isEmpty(activityId)){
                continue;
            }
            var node = nodeMap.get(activityId);

            //only repair B parallel gateway
            var isBackGateWayParallel = NodeTypeEnum.isParallel(node.getType()) && PairPositionEnum.B == node.getPairPosition();
            if(!isBackGateWayParallel){
                continue;
            }
            var isInGateWay = parentParallelGateWayBackIdList.contains(activityId);
            /**
             * 如果不在当前任务的父级网关内，不处理
             * 不在网关内，就不存在修复的情况
             */
            if(!isInGateWay){
                continue;
            }
            /**
             * 是否是下一并行网关节点
             */
            var isEqualsNextGateWay = nextParallelGateWayIdList.contains(activityId);
            if(isEqualsNextGateWay){
                needRepairGateWay.add(activityId);
            }
        }

        for(String activityId : needRepairGateWay){
            repairCount++;
            log.info("repair node is {}",activityId);
            extension.getRuntimeService().createProcessInstanceModification(processInstanceId).startBeforeActivity(activityId).execute();
            log.info("repair process instance id is {} . activity  id  is {}",processInstanceId,activityId);
            repair(processInstanceId,task,repairCount);

        }


    }


    protected void complete(String processInstanceId, String ... directTaskDefinitionKeyList){
        List<Task> taskList = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
        Map<String,Task> taskMap = taskList.stream().collect(Collectors.toMap(Task::getTaskDefinitionKey, i->i));

        Jedis jedis = RedisUtils.getJedis();
        var repairCount = 0;
        if(directTaskDefinitionKeyList == null){
            for(Task task : taskList){
                extension.getTaskService().complete(task.getId());
                if(jedis.get(task.getId()) != null){
                    repair(processInstanceId,task,repairCount);
                }
            }
        }else{
            for (String s : directTaskDefinitionKeyList) {
                Task task = taskMap.get(s);
                if(task != null){
                    extension.getTaskService().complete(task.getId());
                    if(jedis.get(task.getId()) != null){
                        repair(processInstanceId,task,repairCount);
                    }
                }
            }
        }
    }


    /**
     * 获取下一并行网关节点ID列表
     *
     * 这里只获取了并行网关，审批任务和分发任务
     * 我们只需要连续的并行网关
     *
     * for example:
     * GW->GW->AUDIT ==>  GW->GW
     * GW->AUDIT->GW ==>  GW
     *
     * @param nodeList 节点信息
     * @param currentId 指定节点
     * @return List<String>
     */
    protected List<String> findNextParallelGateWayIdList(Collection<Node> nodeList, String currentId){
        var nextNodeList = DiagramHelper.INSTANCE.findNextNodeList(nodeList,currentId);

        nextNodeList = nextNodeList.stream().filter(item->NodeTypeEnum.isParallel(item.getType())
                || NodeTypeEnum.AUDIT == item.getType()
                || NodeTypeEnum.DISTRIBUTION == item.getType()
        ).toList();

        var nextGateWay = new ArrayList<Node>();

        for(Node node : nextNodeList){
            if(NodeTypeEnum.isParallel(node.getType())){
                nextGateWay.add(node);
            }else{
                break;
            }
        }
        return nextGateWay.stream().map(Node::getNodeId).toList();
    }


    protected List<Node> findNextParallelGateWayNodeList(Collection<Node> nodeList, String currentId){
        var nextNodeList = DiagramHelper.INSTANCE.findNextNodeList(nodeList,currentId);

        nextNodeList = nextNodeList.stream().filter(item->NodeTypeEnum.isParallel(item.getType())
                || NodeTypeEnum.AUDIT == item.getType()
                || NodeTypeEnum.DISTRIBUTION == item.getType()
        ).toList();

        var nextGateWay = new ArrayList<Node>();

        for(Node node : nextNodeList){
            if(NodeTypeEnum.isParallel(node.getType())){
                nextGateWay.add(node);
            }else{
                break;
            }
        }
        return nextGateWay;
    }

    protected Node getGateWayPairNode(Node node , List<Node> nodeList){
        var pairId = node.getPairId();
        var nodeGroup = nodeList.stream().filter(item->StringUtils.isNotEmpty(item.getPairId())).collect(Collectors.groupingBy(Node::getPairId));
        var subNodeList = nodeGroup.get(pairId);
        var pairGroup = subNodeList.stream().collect(Collectors.toMap(Node::getPairPosition,i->i));
        if(PairPositionEnum.F == node.getPairPosition()){
            return pairGroup.get(PairPositionEnum.B);
        }else{
            return pairGroup.get(PairPositionEnum.F);
        }
    }

    protected List<Node> getGateWayAuditList(List<Node> nodeList,Node toggleNode){
        var frontNode = PairPositionEnum.F == toggleNode.getPairPosition() ? toggleNode : getGateWayPairNode(toggleNode,nodeList);
        var backNode = getGateWayPairNode(frontNode,nodeList);
        return DiagramHelper.INSTANCE.getGateWayAuditList(frontNode,backNode,nodeList);
    }


    protected void cancelExecution(String processInstanceId ,List<String> cancelActivity,String startActivity){
        if(StringUtils.isNotEmpty(startActivity)){

            ProcessInstanceModificationBuilder builder = extension.getRuntimeService().createProcessInstanceModification(processInstanceId);
            HistoricProcessInstance historicProcessInstance =
                    extension.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            var key = historicProcessInstance.getProcessDefinitionKey();
            var nodeList = NodeTestUtil.buildProcessObject("/bpmn/"+key+".bpmn");

            /**
             * 获取下一并行网关后置节点
             */
            var cancelNodeList =  DiagramHelper.INSTANCE.findNextParallelBackNodeList(nodeList,startActivity);
            if(CollectionUtils.isNotEmpty(cancelNodeList)){
                for(Node backParallel : cancelNodeList){
                    var midAuditNodeList = this.getGateWayAuditList(nodeList,backParallel);
                    var midAuditNodeIdList = midAuditNodeList.stream().map(Node::getNodeId).toList();
                    //判断开始的节点是否在网关之间，如果没在网关之间需要取消
                    if(!midAuditNodeIdList.contains(startActivity)){
                        builder.cancelAllForActivity(backParallel.getNodeId());
                    }
                }
            }

            if(!CollectionUtils.isEmpty(cancelActivity)){
                cancelActivity.forEach(item-> builder.cancelAllForActivity(item));
            }
            builder.startBeforeActivity(startActivity)
                    .setVariable("revoke",true)
                    .setVariable("revoke_gateway",CollectionUtils.isEmpty(cancelActivity));
            builder.execute();
            extension.getRuntimeService().removeVariable(processInstanceId,"revoke");
        }

    }

    protected void contains(String processInstanceId, String ... directTaskDefinitionKeyList){
        List<Task> taskList = extension.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
        List<String> taskDefinitionKeyList = taskList.stream().map(Task::getTaskDefinitionKey).toList();
        Assertions.assertTrue(taskList.size() == directTaskDefinitionKeyList.length);
        for(String taskDefinitionKey : directTaskDefinitionKeyList){
            Assertions.assertTrue(taskDefinitionKeyList.contains(taskDefinitionKey));
        }
    }


}
