package com.yineng.bpe.rpc.order.provider;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * 流程：
 * 提供api接口
 * 通过后置处理器BeanPostProcessor将服务信息放入map中
 * 再通过监听ApplicationListener 监听socket请求
 * 接收到socket请求之后交给 ProcessorHandler 处理
 * ProcessorHandler从服务信息map中取出信息后进行执行返回
 */

@Configuration
@ComponentScan("com.rpc.order.provider")
public class App {

    public static void main( String[] args ){
        ApplicationContext applicationContext= new AnnotationConfigApplicationContext(App.class);
    }
}
