package test.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

public class ParallelExecutionListener implements ExecutionListener {


    @Override
    public void notify(DelegateExecution execution) throws Exception {
        System.out.println(">>>>>>>>>>>>>>>>>> "+execution.getCurrentActivityId());
    }

}
