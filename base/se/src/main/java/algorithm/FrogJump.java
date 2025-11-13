package algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 青蛙跳台
 * 一只青蛙想跳上N阶的楼梯，它可以选择一次跳若干阶。求该青蛙的所有可能跳到顶端的方式
 * 例如台阶N=4，且一次可以跳1或2阶时，有[1, 1, 1, 1], [1, 1, 2], [1, 2, 1], [2, 1, 1], [2, 2]共5种跳法
 * 注意：青蛙视为从第0阶起跳
 * 你应该已经有一个递归的算法frogJump(totalSteps, steps=(1, 2))以实现台阶数=totalSteps、每次可以固定跳steps（此处为1或者2）情况下的跳法
 * 问题1：不改变steps参数，改进frogJump将其优化到O(totalSteps)的时间
 * 问题2：你在回答问题1时的解决方案占用了多少空间？与totalStep有什么关系？
 * 问题3：改进frogJump将其优化到O(totalSteps)的时间，对任何合理的steps参数都生效。（如果你问题1回答得够好，你可能已经完成了这个问题）
 *
 * @author lixf
 */
public class FrogJump {

    public int frogJumpCommon(int n){
        if(n<2){
            return 1;
        }
        return frogJumpCommon(n-1) + frogJumpCommon(n -2);
    }

    public long frogJump(long totalSteps, long [] steps, Map<Long,Long> cacheMap){
        if(cacheMap.containsKey(totalSteps)){
            return cacheMap.get(totalSteps);
        }
        if(totalSteps<2){
            return 1;
        }
        long perTotalSteps = 0;
        for (long step : steps) {
            if(totalSteps >= step){
                perTotalSteps += frogJump(totalSteps - step,steps,cacheMap);
                cacheMap.put(totalSteps,perTotalSteps);
            }
        }
        return perTotalSteps;
    }

    public static void main(String[] args) {
        FrogJump frogJump = new FrogJump();
//        System.out.println(frogJump.frogJumpCommon(50));
        Map<Long,Long> map = new HashMap<>();
        System.out.println(frogJump.frogJump(100,new long[]{1,2},map));
    }

}
