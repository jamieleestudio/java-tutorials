package test.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

@Slf4j
public class UserTaskListener implements TaskListener {


    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("revoke >>>>>>>>>>"+delegateTask.getVariable("revoke")+">>>>>>>>>>> and task id is "+delegateTask.getId());
        Object object = delegateTask.getVariable("revoke");
        Object revokeGateWay = delegateTask.getVariable("revoke_gateway");
        if(object != null && revokeGateWay != null){
            boolean flag = Boolean.valueOf(String.valueOf(revokeGateWay));
            if(!flag){
                String taskId = delegateTask.getId();
//                RedisUtils.getJedis().set(taskId,taskId);
            }
        }
    }

}
