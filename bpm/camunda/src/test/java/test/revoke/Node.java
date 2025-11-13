package test.revoke;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;

/** 流程节点 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Node extends SimpleNode {

    /** 节点配置 */
    private LinkedHashMap<String, Object> nodeDetails;

}
