package com.third.li.liteflow;

import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.core.FlowExecutorHolder;
import com.yomahub.liteflow.flow.LiteflowResponse;
import com.yomahub.liteflow.property.LiteflowConfig;
import com.yomahub.liteflow.slot.DefaultContext;

public class LiteFlowApp {

    public static void main(String[] args) {
        LiteflowConfig config = new LiteflowConfig();
        config.setRuleSource("config/flow.xml");
        FlowExecutor flowExecutor = FlowExecutorHolder.loadInstance(config);

        LiteflowResponse response = flowExecutor.execute2Resp("chain1", "arg", DefaultContext.class);
        System.out.println(response.isSuccess());
    }

}
