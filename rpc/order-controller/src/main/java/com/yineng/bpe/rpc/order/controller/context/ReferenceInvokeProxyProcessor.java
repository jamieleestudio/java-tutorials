package com.yineng.bpe.rpc.order.controller.context;

import com.yineng.bpe.rpc.order.controller.annotation.Reference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

//spring 后置处理
@Component
public class ReferenceInvokeProxyProcessor implements BeanPostProcessor {

    @Autowired
    private RemoteInvocationHandler invocationHandler;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        Field[] fields=bean.getClass().getDeclaredFields();
        for(Field field:fields){
            //如果字段中有Reference注解为字段添加代理
            if(field.isAnnotationPresent(Reference.class)){
                field.setAccessible(true);
                //针对这个加了Reference注解的字段，设置为一个代理的值
                //invocationHandler 实现方法 远程发送参数
                Object proxy= Proxy.newProxyInstance(field.getType().getClassLoader(),new Class<?>[]{field.getType()},invocationHandler);
                try {
                    field.set(bean,proxy); //相当于针对加了Reference的注解，设置了一个代理，这个代理的实现是inovcationHandler
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

}
