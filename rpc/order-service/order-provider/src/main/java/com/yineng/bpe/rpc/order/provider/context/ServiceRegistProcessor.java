package com.yineng.bpe.rpc.order.provider.context;

import com.yineng.bpe.rpc.order.provider.annotation.RemoteService;
import com.yineng.bpe.rpc.order.provider.bean.BeanMethod;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

//spring 后置处理
@Component
public class ServiceRegistProcessor implements BeanPostProcessor {

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        //如果是远程服务
        if(bean.getClass().isAnnotationPresent(RemoteService.class)){

            Method[] methods=bean.getClass().getDeclaredMethods();
            for(Method method:methods){
                String key=bean.getClass().getInterfaces()[0].getName()+"."+method.getName();

                //将封装后的方法信息放入map中
                BeanMethodMediator.map.put(key,new BeanMethod(bean,method));
            }
        }

        return bean;
    }

}
