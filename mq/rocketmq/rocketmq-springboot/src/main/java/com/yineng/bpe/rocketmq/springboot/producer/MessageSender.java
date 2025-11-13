package com.yineng.bpe.rocketmq.springboot.producer;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MessageSender {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void syncSend(){
        /**
         * 发送可靠同步消息 ,可以拿到SendResult 返回数据
         * 同步发送是指消息发送出去后，会在收到mq发出响应之后才会发送下一个数据包的通讯方式。
         * 这种方式应用场景非常广泛，例如重要的右键通知、报名短信通知、营销短信等。
         *
         * 参数1： topic:tag
         * 参数2:  消息体 可以为一个对象
         * 参数3： 超时时间 毫秒
         */
        SendResult result= rocketMQTemplate.syncSend("springboot-topic:tag","这是一条同步消息",10000);
        System.out.println(result);
    }

    /**
     * 发送 可靠异步消息
     * 发送消息后，不等mq响应，接着发送下一个数据包。发送方通过设置回调接口接收服务器的响应，并可对响应结果进行处理。
     * 异步发送一般用于链路耗时较长，对于RT响应较为敏感的业务场景，例如用户上传视频后通过启动转码服务，转码完成后通推送转码结果。
     *
     * 参数1： topic:tag
     * 参数2:  消息体 可以为一个对象
     * 参数3： 回调对象
     */
    public void asyncSend() throws Exception{

        rocketMQTemplate.asyncSend("springboot-topic:tag1", "这是一条异步消息", new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {

                System.out.println("回调sendResult:"+sendResult);
            }

            @Override
            public void onException(Throwable e) {
                System.out.println(e.getMessage());
            }
        });
        TimeUnit.SECONDS.sleep(100000);
    }

    /**
     * 发送单向消息
     * 参数1： topic:tag
     * 参数2:  消息体 可以为一个对象
     */
    public void sendOneWay(){

        rocketMQTemplate.sendOneWay("springboot-topic:tag1", "这是一条单向消息");
    }


    /**
     * 发送单向的顺序消息
     */
    public void sendOneWayOrderly(){
        for(int i=0;i<10;i++){

            rocketMQTemplate.sendOneWayOrderly("springboot-topic:tag1", "这是一条顺序消息"+i,"2673");
            rocketMQTemplate.sendOneWayOrderly("springboot-topic:tag1", "这是一条顺序消息"+i,"2673");
        }
    }

}
