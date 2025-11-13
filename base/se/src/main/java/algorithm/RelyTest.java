package algorithm;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lixf
 */
public class RelyTest {


    public static final List<Snode> nodeList = new ArrayList<>();

    static {
        nodeList.add(new Snode("1","2"));
        nodeList.add(new Snode("1","5"));
        nodeList.add(new Snode("2","3"));
        nodeList.add(new Snode("3","4"));
        nodeList.add(new Snode("4","5"));
        nodeList.add(new Snode("5","6"));
        nodeList.add(new Snode("5","7"));
        nodeList.add(new Snode("6","1"));
    }

    public static void main(String[] args) {
        //

        buildNodeList(nodeList,"2");

    }


    private static void buildNodeList(List<Snode> snodeList,String id){

        Map<String,List<Snode>> stringListMap = snodeList.stream().collect(Collectors.groupingBy(Snode::getId));



        Node node = new Node();
        node.setId(id);

        Set<String> existsIdList = new HashSet<>();
        existsIdList.add(id);

        buildNodeList2(stringListMap,node,existsIdList);
        System.out.println(node);

//        foreach(node.getNextList());
    }


    private static void foreach(List<Node> nextList){
        for(Node n: nextList){
            System.out.println(n);
            while (n.hasNextList()){
                foreach(n.getNextList());
            }
        }
    }



    private static  void buildNodeList2(Map<String,List<Snode>> stringListMap,
                                        Node startNode,
                                        Set<String> existsIdList){

        //节点下的子节点
        List<Snode> substartSnodeList = stringListMap.get(startNode.getId());
        //遍历子节点
        List<Node> subNodeList;

        if(CollectionUtils.isNotEmpty(substartSnodeList)){
            subNodeList = new ArrayList<>();
            for(Snode snode1 : substartSnodeList){
                subNodeList.add(new Node(snode1.getTarget()));
            }
            startNode.setNextList(subNodeList);

            for(Node node1 : subNodeList){

                if(existsIdList.add(node1.getId())){
                    buildNodeList2(stringListMap,node1,existsIdList);
                }else{
                    System.out.println("已经存在");
                }

            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node{

        public Node(String id){
            this.id = id;
        }

        private String id;

        private List<Node> nextList;

        private boolean hasNextList(){
            return CollectionUtils.isNotEmpty(nextList);
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Snode{
        private String id;
        private String target;
    }

}
