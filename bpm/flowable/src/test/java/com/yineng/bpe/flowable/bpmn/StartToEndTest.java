package com.yineng.bpe.flowable.bpmn;

import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class StartToEndTest extends PluggableFlowableTestCase {

    @Test
    @Deployment
    public void testStartToEnd() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startToEnd");
        assertProcessEnded(processInstance.getId());
        assertThat(processInstance.isEnded()).isTrue();
    }

}
