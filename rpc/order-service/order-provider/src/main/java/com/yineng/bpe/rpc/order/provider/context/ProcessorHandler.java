package com.yineng.bpe.rpc.order.provider.context;


import com.yineng.bpe.rpc.order.api.request.RpcRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ProcessorHandler implements Runnable{

    private final Socket socket;

    public ProcessorHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        ObjectInputStream inputStream=null;
        ObjectOutputStream outputStream=null;

        try {
            //获取对象输入流
            inputStream=new ObjectInputStream(socket.getInputStream());//

            RpcRequest request=(RpcRequest)inputStream.readObject(); //反序列化
            //路由
            BeanMethodMediator mediator=BeanMethodMediator.getInstance();
            //进行执行方法
            Object rs=mediator.processor(request);
            //输出返回结果
            outputStream=new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(rs);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
