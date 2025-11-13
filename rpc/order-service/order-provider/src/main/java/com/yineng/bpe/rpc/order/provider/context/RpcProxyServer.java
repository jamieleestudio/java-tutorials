package com.yineng.bpe.rpc.order.provider.context;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RpcProxyServer {

    private final ExecutorService executorService= Executors.newCachedThreadPool();

    public void publisher(Object service,int port){
        ServerSocket serverSocket=null;
        try {
            serverSocket=new ServerSocket(port);
            while(true){
                Socket socket=serverSocket.accept(); //监听客户端请求
                executorService.execute(new ProcessorHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(serverSocket!=null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
