package test.revoke;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 流程图帮助类
 */
public enum DiagramHelper {

    INSTANCE;


    /**
     * 获取父级并行网关后置几点列表（指定节点在网关内）
     * @param nodeList 节点列表
     * @param currentNodeId 指定节点ID
     * @return List<Node>
     */
    public List<Node> findParentParallelGateWayBackList(Collection<Node> nodeList, String currentNodeId){
        return findParentParallelGateWayList(nodeList,currentNodeId).stream().filter(item-> PairPositionEnum.B == item.getPairPosition()).toList();
    }


    /**
     * 获取父级并行网关列表（指定节点在网关内）
     * @param nodeList 节点列表
     * @param currentNodeId 指定节点ID
     * @return List<Node>
     */
    public List<Node> findParentParallelGateWayList(Collection<Node> nodeList, String currentNodeId){

        Map<String,List<Node>> pairGroup = nodeList.stream()
                .filter(item-> NodeTypeEnum.isParallel(item.getType()) &&  StringUtils.isNotEmpty(item.getPairId()))
                .collect(Collectors.groupingBy(Node::getPairId));

        List<Node> parentNodeList =  Lists.newArrayList();

        for(Map.Entry<String,List<Node>> pairEntry : pairGroup.entrySet() ){
            var pairNode = pairEntry.getValue();
            var pairNodeMap = pairNode.stream().collect(Collectors.toMap(Node::getPairPosition,i->i));

            var front = pairNodeMap.get(PairPositionEnum.F);
            var back = pairNodeMap.get(PairPositionEnum.B);

            var midAuditList = getGateWayAuditList(front,back,nodeList);
            var midAuditIdList = midAuditList.stream().map(Node::getNodeId).toList();

            if(midAuditIdList.contains(currentNodeId)){
                parentNodeList.add(front);
                parentNodeList.add(back);
            }
        }
        return parentNodeList;
    }

    public List<Node> findNextNodeList(Collection<Node> nodeList, String currentNodeId){
        Map<String,Node> nodeMap = nodeList.stream().collect(Collectors.toMap(Node::getNodeId, item->item));
        //退回的目标节点
        Node flow = nodeMap.get(currentNodeId);
        List<Node> newNodeList =  Lists.newArrayList();
        addAllNode(nodeList,flow,newNodeList);
        return newNodeList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 获取父级节点（网关）
     * @param nodeList 节点信息
     * @param currentNodeId 当前节点
     */
    public  Node findParentGateWayNode(Collection<Node> nodeList, String currentNodeId){
        Map<String,Node> nodeMap = nodeList.stream().collect(Collectors.toMap(Node::getNodeId, item->item));
        Node currentNode  = nodeMap.get(currentNodeId);
        List<Node> parentNodeList =  Lists.newArrayList();
        findParentNode(nodeList,currentNode,parentNodeList);
        List<Node> gateWayNodeList = parentNodeList.stream()
                .filter(item->NodeTypeEnum.GW_EXCLUSIVE == item.getType() || NodeTypeEnum.GW_PARALLEL == item.getType()).toList();
        if(CollectionUtils.isEmpty(gateWayNodeList)){
            return null;
        }
        if(gateWayNodeList.size() > 1){
//            throw BusinessException.wrap(ProcessRuntimeErrorCode.NOT_SUPPORT_OP);
        }
        return gateWayNodeList.iterator().next();
    }


    private  void findParentNode(Collection<Node>  sourceNodeList, Node node, List<Node> resultNodeList){
        sourceNodeList.forEach(item->{
            if(CollectionUtils.isNotEmpty(item.getIncomingList())){
                //如果某个节点的outgoing是
                if(item.getOutgoingList().contains(node.getNodeId())){
                    resultNodeList.add(item);
                    //如果不是审核节点和网关则继续往下面找
                    if(NodeTypeEnum.GW_PARALLEL !=  item.getType()
                            && NodeTypeEnum.GW_EXCLUSIVE != item.getType()
                            && NodeTypeEnum.AUDIT != item.getType()){
                        findParentNode(sourceNodeList, item,resultNodeList);
                    }
                }
            }
        });
    }

    /**
     * 获取下一审核节点列表
     * @param nodeList 节点列表
     * @param currentNodeId 当前节点ID
     * @return List<Node>
     */
    public  List<Node> findNextAuditNodeList(List<Node> nodeList, String currentNodeId){
        Map<String,Node> nodeMap = nodeList.stream().collect(Collectors.toMap(Node::getNodeId, item->item));
        //退回的目标节点
        Node flow = nodeMap.get(currentNodeId);
        List<Node> newNodeList =  Lists.newArrayList();

        addNode(nodeList,flow,newNodeList);
        clean(newNodeList);

        newNodeList = newNodeList.stream()
                .filter(item-> NodeTypeEnum.AUDIT == item.getType() || NodeTypeEnum.DISTRIBUTION == item.getType()).toList();
        return newNodeList.stream().distinct().collect(Collectors.toList());
    }


    public boolean gatewayHasEmptyLine(Node f,Node b,List<Node> nodeList){
        var nodeMap = nodeList.stream().collect(Collectors.toMap(Node::getNodeId,i->i));
        for(var outgo : f.getOutgoingList()){
            var hasEmptyLine = this.hasEmptyLine(nodeMap.get(outgo),b,nodeList);
            if(hasEmptyLine){
                return true;
            }
        }
        return false;
    }


    private boolean hasEmptyLine(Node f,Node b,List<Node> nodeList){
        for(Node item : nodeList){
            if(item.getIncomingList().contains(f.getNodeId())){
                var isAudit = NodeTypeEnum.AUDIT == item.getType() || NodeTypeEnum.DISTRIBUTION == item.getType();
                if(isAudit){
                    return true;
                }
                if(!item.getOutgoingList().contains(b.getNodeId()) ){
                    hasEmptyLine(item,b,nodeList);
                }
            }
        }
        return false;
    }

    public List<Node> getGateWayAuditList(Node f,Node b,Collection<Node> nodeList){
        List<Node> resultNodeList  = Lists.newArrayList();
        addGateWayHasAudit(f,b,nodeList,resultNodeList);
        if(CollectionUtils.isEmpty(resultNodeList)){
            return Lists.newArrayList();
        }
        resultNodeList = resultNodeList.stream()
                .filter(item->NodeTypeEnum.AUDIT == item.getType() || NodeTypeEnum.DISTRIBUTION == item.getType()).toList();
        return resultNodeList;
    }

    private boolean gatewayHasAudit(Node f,Node b,List<Node> nodeList){
        return CollectionUtils.isNotEmpty(this.getGateWayAuditList(f,b,nodeList));
    }


    private void addGateWayHasAudit(Node f,Node b ,Collection<Node> nodeList ,Collection<Node> resultNodeList){
        nodeList.forEach(item->{
            if(item.getIncomingList().contains(f.getNodeId())){
                resultNodeList.add(item);
                if(!item.getOutgoingList().contains(b.getNodeId())){
                    addGateWayHasAudit(item,b,nodeList,resultNodeList);
                }
            }
        });
    }

    private void clean(List<Node> nodeList){

        var pairGroup = nodeList.stream().distinct()
                .filter(
                        item-> StringUtils.isNotEmpty(item.getPairId())
                                && NodeTypeEnum.GW_PARALLEL == item.getType()
                )
                .collect(Collectors.groupingBy(Node::getPairId));
        if(MapUtils.isEmpty(pairGroup)){
            return;
        }

        List<Node> cleanNode = Lists.newArrayList();

        for(Map.Entry<String,List<Node>> pairGroupEntry :  pairGroup.entrySet()){
            var groupNodeList = pairGroupEntry.getValue();
            var size = groupNodeList.size();
            if(size == 2){
                var pairMap = groupNodeList.stream().collect(Collectors.toMap(Node::getPairPosition,i->i));
                var nodeF = pairMap.get(PairPositionEnum.F);
                var nodeB = pairMap.get(PairPositionEnum.B);
                if(!gatewayHasAudit(nodeF,nodeB,nodeList)){
                    continue;
                }
                Node backNode = groupNodeList.stream().collect(Collectors.toMap(Node::getPairPosition,i->i))
                        .get(PairPositionEnum.B);
                cleanNode.add(backNode);
            }
        }
        Set<String> removeIds  = Sets.newHashSet();
        for(Node node : cleanNode){
            removeNode(node,nodeList,removeIds);
        }
        if(CollectionUtils.isNotEmpty(removeIds)){
            nodeList.removeIf(node->removeIds.contains(node.getNodeId()));
        }
    }

    private void removeNode(Node cleanNode , List<Node> nodeList ,Set<String> removeIds){
        nodeList.forEach(item->{
            if(item.getIncomingList().contains(cleanNode.getNodeId())){
                removeIds.add(item.getNodeId());
                removeNode(item,nodeList,removeIds);
            }
        });
    }


    private  void addAllNode(Collection<Node>  sourceNodeList, Node node, List<Node> nodeList){
        sourceNodeList.forEach(item->{
            if(CollectionUtils.isNotEmpty(item.getOutgoingList())){
                //如果某个节点的输入节点是指定的那个节点,说明这个节点就是那个节点的下一个节点
                if(item.getIncomingList().contains(node.getNodeId())){
                    nodeList.add(item);
                    addAllNode(sourceNodeList, item,nodeList);
                }
            }
        });
    }


    private  void addNode(List<Node>  sourceNodeList, Node node, List<Node> nodeList){

        sourceNodeList.forEach(item->{
            if(CollectionUtils.isNotEmpty(item.getOutgoingList())){
                //如果某个节点的输入节点是指定的那个节点,说明这个节点就是那个节点的下一个节点
                if(item.getIncomingList().contains(node.getNodeId())){
                    nodeList.add(item);
                    //如果不是审核节点则继续往下面找
                    if(NodeTypeEnum.AUDIT !=  item.getType()){
                        addNode(sourceNodeList, item,nodeList);
                    }
                }
            }
        });
    }

    public  List<Node> searchBackNodeList(List<Node> nodeList,String backNodeId){
        Map<String,Node>  nodeMap = nodeList.stream().collect(Collectors.toMap(Node::getNodeId,item->item));
        //退回的目标节点
        Node flow = nodeMap.get(backNodeId);
        List<Node> newNodeList =  Lists.newArrayList();

        addSNode(nodeList,flow,newNodeList);
        //去掉线和网关的back
        newNodeList = newNodeList.stream().filter(item->
                NodeTypeEnum.FLOW != item.getType() && PairPositionEnum.B !=item.getPairPosition()).collect(Collectors.toList());
        return newNodeList.stream().distinct().collect(Collectors.toList());
    }


    public List<Node> findNextParallelBackNodeList(List<Node> nodeList,String currentId){
        var  nodeMap = nodeList.stream().collect(Collectors.toMap(Node::getNodeId,item->item));
        var currentNode = nodeMap.get(currentId);
        List<Node> resultList =  Lists.newArrayList();

        addSNode(nodeList,currentNode,resultList);
        resultList = resultList.stream()
                .filter(item-> NodeTypeEnum.isParallel(item.getType())&& PairPositionEnum.B ==item.getPairPosition())
                .collect(Collectors.toList());
        return resultList.stream().distinct().collect(Collectors.toList());
    }


    public  List<Node> searchBackNodeListRevoke(List<Node> nodeList,String backNodeId){
        Map<String,Node>  nodeMap = nodeList.stream().collect(Collectors.toMap(Node::getNodeId,item->item));
        //退回的目标节点
        Node flow = nodeMap.get(backNodeId);
        List<Node> newNodeList =  Lists.newArrayList();

        addSNode(nodeList,flow,newNodeList);
        //去掉线和网关的back
        newNodeList = newNodeList.stream().filter(item->
                NodeTypeEnum.GW_PARALLEL == item.getType() && PairPositionEnum.B ==item.getPairPosition()).collect(Collectors.toList());
        return newNodeList.stream().distinct().collect(Collectors.toList());
    }

    private  void addSNode(List<Node>  sourceNodeList,Node node,List<Node> nodeList){

        sourceNodeList.forEach(item->{

            if(CollectionUtils.isNotEmpty(item.getOutgoingList())){
                //如果某个节点的输入节点是指定的那个节点,说明这个节点就是那个节点的下一个节点
                if(item.getIncomingList().contains(node.getNodeId())){
                    nodeList.add(item);
                    addSNode(sourceNodeList, item,nodeList);
                }
            }
        });
    }



}
