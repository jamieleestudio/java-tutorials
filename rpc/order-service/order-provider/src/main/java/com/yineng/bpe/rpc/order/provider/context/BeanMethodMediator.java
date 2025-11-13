package com.yineng.bpe.rpc.order.provider.context;


import com.yineng.bpe.rpc.order.api.request.RpcRequest;
import com.yineng.bpe.rpc.order.provider.bean.BeanMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanMethodMediator {

    public static Map<String , BeanMethod> map=new ConcurrentHashMap<>();

    private static BeanMethodMediator INSTANCE = new BeanMethodMediator();

    private BeanMethodMediator(){}

    public static BeanMethodMediator getInstance(){
        return INSTANCE;
    }

    public Object processor(RpcRequest request){

        String key = String.format("%s.%s",request.getClassName(),request.getMethodName());

        //获取方法封装
        BeanMethod beanMethod=map.get(key);
        if(beanMethod==null){
            return null;
        }
        Object bean=beanMethod.getBean();
        Method method=beanMethod.getMethod();
        try {
            //执行方法
            return method.invoke(bean,request.getArgs());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
