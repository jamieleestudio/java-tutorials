package com.yineng.bpe.rpc.order.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 流程：
 * BeanPostProcessor 后置处理器将 @Reference 注解的对象动态代理 OrderController 中的 OrderService
 * 设置了代理之后实现类为代理类
 * 代理类获取调用的方法再进行远端发送
 * 等待后端返回
 */
@SpringBootApplication
public class AppConsumer {
    public static void main( String[] args ){
        SpringApplication.run(AppConsumer.class,args);
    }
}
