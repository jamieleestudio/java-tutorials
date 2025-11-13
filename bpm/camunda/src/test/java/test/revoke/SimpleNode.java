package test.revoke;

import lombok.Data;

import java.util.List;

/**
 *
 * 节点节本属性
 *
 * @author lixf
 */
@Data
public class SimpleNode {

    /**
     * 节点ID
     */
    private String nodeId;

    /** 节点名称 */
    private String nodeName;
    /**
     * 节点类型
     */
    private NodeTypeEnum type;

    /**
     * 成对ID
     */
    private String pairId;

    /**
     * 成对位置
     */
    private PairPositionEnum pairPosition;


    /** 输入节点 */
    private List<String> incomingList;
    /** 输出节点 */
    private List<String> outgoingList;

}
