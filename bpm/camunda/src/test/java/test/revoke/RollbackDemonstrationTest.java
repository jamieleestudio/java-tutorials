package test.revoke;

import com.google.common.collect.Lists;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import test.CamundaBusinessProcessConfigurationTest;

public class RollbackDemonstrationTest extends CamundaBusinessProcessConfigurationTest {

    /**
     * Demonstrates rollback in a parallel gateway scenario using existing cancelExecution logic.
     * Process: Start -> A_1 -> Parallel Gateway -> (A_2, A_3) -> Parallel Gateway -> A_4 -> End
     */
    @Test
    public void testParallelGatewayRollback() {
        // 1. Start process
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_simple");
        String processInstanceId = processInstance.getProcessInstanceId();

        // 2. Complete A_1, enters parallel gateway. A_2 and A_3 should be active.
        complete(processInstanceId, "A_1");
        contains(processInstanceId, "A_2", "A_3");

        // --- Scenario 1: Rollback from inside parallel (A_2) to before parallel (A_1) ---
        // We want to cancel A_2 (and A_3 implicitly if we want a clean state) and go back to A_1.
        // The cancelExecution method logic seems to handle "gateway scope" if we configure it right.
        // Let's try canceling both A_2 and A_3 and starting A_1.
        cancelExecution(processInstanceId, Lists.newArrayList("A_2", "A_3"), "A_1");

        // Verify we are back at A_1 only
        contains(processInstanceId, "A_1");

        // Move forward again
        complete(processInstanceId, "A_1");
        contains(processInstanceId, "A_2", "A_3");

        // --- Scenario 2: Complete A_2 and A_3, reach A_4. Then rollback to A_2. ---
        complete(processInstanceId, "A_2", "A_3");
        contains(processInstanceId, "A_4");

        // Rollback from A_4 to A_2.
        // Note: restarting A_2 might leave the process in a state where it waits for A_3 again at the joining gateway?
        // Let's see how Camunda handles this.
        cancelExecution(processInstanceId, Lists.newArrayList("A_4"), "A_2");

        // We expect A_2 to be active.
        contains(processInstanceId, "A_2");
        
        // If we complete A_2 now, it should wait at the joining gateway.
        complete(processInstanceId, "A_2");
        
        // But A_3 was already completed in the previous "pass" (before rollback). 
        // Does the history/token state remember that? 
        // Or do we need to restart A_3 as well if we want to "redo" the parallel block?
        // If we just restart A_2, the token for A_3 from the previous pass is already consumed by the joining gateway?
        // Actually, when we cancelled A_4, the token was at A_4. The joining gateway had already fired.
        // So putting a token at A_2 means we have one token moving to the joining gateway.
        // The joining gateway needs tokens from both branches to fire again.
        // Unless the joining gateway logic is complex, we probably need to restart A_3 as well OR manually simulate the token for A_3 arriving.
        
        // Let's check what happens. If the test fails here, it's an educational moment about BPMN semantics.
        // If I want to fully "redo" the parallel block, I should probably restart A_2 AND A_3.
        
        // Let's try just A_2 first as written above.
    }
    
    @Test
    public void testParallelGatewayRollback_FullReset() {
         // 1. Start process
        ProcessInstance processInstance = extension.getRuntimeService().startProcessInstanceByKey("pa_simple");
        String processInstanceId = processInstance.getProcessInstanceId();
        
        complete(processInstanceId, "A_1");
        complete(processInstanceId, "A_2", "A_3");
        contains(processInstanceId, "A_4");
        
        // Rollback from A_4 to A_2 AND A_3 to fully reset the parallel gateway execution
        cancelExecution(processInstanceId, Lists.newArrayList("A_4"), "A_2");
        // We also want A_3 to be active, so we might need to modify how we call it.
        // cancelExecution takes a single startActivity. 
        // If we want to start multiple, we might need multiple calls or the method needs to support it.
        // The current signature is `cancelExecution(String processInstanceId ,List<String> cancelActivity,String startActivity)`
        // It only supports one start activity.
        
        // So if we start A_2, we get one token.
        // Then we can use the API directly to also start A_3 if needed.
        extension.getRuntimeService().createProcessInstanceModification(processInstanceId).startBeforeActivity("A_3").execute();
        
        contains(processInstanceId, "A_2", "A_3");
        
        complete(processInstanceId, "A_2", "A_3");
        contains(processInstanceId, "A_4");
    }
}
