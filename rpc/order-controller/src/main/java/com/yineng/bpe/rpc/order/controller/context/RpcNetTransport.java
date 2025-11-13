package com.yineng.bpe.rpc.order.controller.context;



import com.yineng.bpe.rpc.order.api.request.RpcRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//rpc传输
public class RpcNetTransport {

    //地址
    private final String host;
    //端口
    private final int    port;

    public RpcNetTransport(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Socket newSocket() throws IOException {
        return new Socket(host,port);
    }

    public Object send(RpcRequest request){
        ObjectOutputStream outputStream=null;
        ObjectInputStream inputStream=null;
        try {
            Socket socket=newSocket();
            //发送数据
            outputStream=new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(request);
            outputStream.flush();

            //接受返回数据
            inputStream=new ObjectInputStream(socket.getInputStream());
            return inputStream.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {

            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
