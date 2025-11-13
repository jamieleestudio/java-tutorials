package test.revoke;

/**
 * 节点枚举
 *
 * @author lixf
 */
public enum NodeTypeEnum {

    /**
     * 开始节点
     */
    START,

    /**
     * 发起节点
     */
    LAUNCH,
    /**
     * 审核节点
     */
    AUDIT,

    /**
     * 分发节点
     */
    DISTRIBUTION,
    /**
     * 平行网关节点
     */
    GW_PARALLEL,

    GW_INCLUSIVE,

    /**
     * 排他网关节点
     */
    GW_EXCLUSIVE,

    /**
     * 抄送
     */
    CARBON_COPY,

    /**
     * 连线
     */
    FLOW,

    /**
     * 结束节点
     */
    END;


    public static boolean isGateWay(NodeTypeEnum nodeTypeEnum){
        return nodeTypeEnum == NodeTypeEnum.GW_INCLUSIVE || nodeTypeEnum == NodeTypeEnum.GW_EXCLUSIVE
                || nodeTypeEnum == NodeTypeEnum.GW_PARALLEL;
    }

    public static boolean isParallel(NodeTypeEnum nodeTypeEnum){
        return nodeTypeEnum == NodeTypeEnum.GW_INCLUSIVE || nodeTypeEnum == NodeTypeEnum.GW_PARALLEL;
    }

}

