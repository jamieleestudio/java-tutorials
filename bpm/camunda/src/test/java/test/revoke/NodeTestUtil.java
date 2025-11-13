package test.revoke;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class NodeTestUtil {

    public static List<Node> buildProcessObject(String bpmnPath)  {
        InputStream inputStream = NodeTestUtil.class.getResourceAsStream(bpmnPath);

        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(inputStream);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        Element rootElement = document.getRootElement();
        List<Element> elements = rootElement.element("process").elements();   //element 节点

        List<Node> revokeNodeList = Lists.newArrayList();

        for(Element element : elements){

            Node node = new Node();
            node.setNodeId(element.attributeValue("id"));

            List<Element> outgoingList =  element.elements("outgoing");
            List<String>  outgoingIdList = Lists.newArrayList();
            if(CollectionUtils.isNotEmpty(outgoingList)){
                outgoingIdList = outgoingList.stream().map(Element::getText).collect(Collectors.toList());
            }
            List<Element> incomingList =  element.elements("incoming");
            List<String>  incomingIdList = Lists.newArrayList();
            if(CollectionUtils.isNotEmpty(incomingList)){
                incomingIdList = incomingList.stream().map(Element::getText).collect(Collectors.toList());
            }

            node.setIncomingList(incomingIdList);
            node.setOutgoingList(outgoingIdList);

            String name = element.getName();
            switch (name) {
                case "startEvent" -> node.setType(NodeTypeEnum.START);
                case "sequenceFlow" -> {
                    node.setType(NodeTypeEnum.FLOW);
                    String sourceRef = element.attributeValue("sourceRef");
                    String targetRef = element.attributeValue("targetRef");
                    node.setIncomingList(Lists.newArrayList(sourceRef));
                    node.setOutgoingList(Lists.newArrayList(targetRef));
                }
                case "exclusiveGateway" -> {
                    node.setType(NodeTypeEnum.GW_EXCLUSIVE);
                    Attribute attribute = element.attribute("pair");
                    if (attribute != null) {
                        node.setPairId(attribute.getText());
                        node.setPairPosition(PairPositionEnum.valueOf(element.attributeValue("position")));
                    }
                }

                case "inclusiveGateway" -> {
                    node.setType(NodeTypeEnum.GW_INCLUSIVE);
                    Attribute attribute = element.attribute("pair");
                    if (attribute != null) {
                        node.setPairId(attribute.getText());
                        node.setPairPosition(PairPositionEnum.valueOf(element.attributeValue("position")));
                    }
                }

                case "parallelGateway" ->{
                    node.setType(NodeTypeEnum.GW_PARALLEL);
                    Attribute attribute = element.attribute("pair");
                    if (attribute != null) {
                        node.setPairId(attribute.getText());
                        node.setPairPosition(PairPositionEnum.valueOf(element.attributeValue("position")));
                    }
                }
                case "endEvent" -> node.setType(NodeTypeEnum.END);
                case "task", "userTask" -> node.setType(NodeTypeEnum.AUDIT);
            }
            revokeNodeList.add(node);
        }
        return revokeNodeList;
    }


}
