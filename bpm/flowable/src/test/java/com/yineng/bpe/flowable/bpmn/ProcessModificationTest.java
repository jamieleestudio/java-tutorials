package com.yineng.bpe.flowable.bpmn;

import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 演示如何修改运行中的流程实例（跳转/更改活动状态）
 */
public class ProcessModificationTest extends PluggableFlowableTestCase {

    @Test
    @Deployment
    public void testProcessModification() {
        // 1. 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processModification");
        
        // 2. 验证当前在 Task 1
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("task1");
        assertThat(task.getName()).isEqualTo("Task 1");
        
        // 3. 将流程执行从 Task 1 跳转到 Task 2 (跳过任务)
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("task1", "task2")
                .changeState();
        
        // 4. 验证当前已经在 Task 2
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("task2");
        assertThat(task.getName()).isEqualTo("Task 2");
        
        // 5. 将流程执行从 Task 2 跳回 Task 1 (回退)
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdTo("task2", "task1")
                .changeState();
                
        // 6. 验证当前又回到了 Task 1
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("task1");
        assertThat(task.getName()).isEqualTo("Task 1");
        
        // 7. 正常完成 Task 1
        taskService.complete(task.getId());
        
        // 8. 验证到达 Task 2
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task.getTaskDefinitionKey()).isEqualTo("task2");
        
        // 9. 完成 Task 2，流程结束
        taskService.complete(task.getId());
        
        assertProcessEnded(processInstance.getId());
    }
}
