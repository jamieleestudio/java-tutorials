package com.yineng.bpe.rpc.order.controller.context;

import com.yineng.bpe.rpc.order.api.request.RpcRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

//代理处理器
//此处不是直接调用方法，而是远程发送数据
@Component
public class RemoteInvocationHandler implements InvocationHandler{

    @Value("${rpc.host}")
    private String host;

    @Value("${rpc.port}")
    private int port;

    public RemoteInvocationHandler() {
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        //先建立远程连接
        RpcNetTransport rpcNetTransport=new RpcNetTransport(host,port);
        //传递数据了？
        // 调用哪个接口、 哪个方法、方法的参数？
        RpcRequest request=new RpcRequest();
        request.setArgs(args);
        request.setClassName(method.getDeclaringClass().getName());
        request.setTypes(method.getParameterTypes()); //参数的类型
        request.setMethodName(method.getName());
        return rpcNetTransport.send(request);
    }
}
