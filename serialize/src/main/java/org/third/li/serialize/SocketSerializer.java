package org.third.li.serialize;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class SocketSerializer {

    //客户端
    public static class SocketClient{

        public void request(User user){
            try {
                Socket socket=new Socket("localhost",8080);
                //如何传递？
                ObjectOutputStream outputStream=new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(user);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                //TODO
            }
        }

        public static void main(String[] args) {
            new SocketClient().request(new User("li",18));
        }

    }

    //服务端
    public static class SocketServer{

        public void run(){
            ServerSocket serverSocket=null;
            try {
                serverSocket=new ServerSocket(8080);
                Socket socket=serverSocket.accept();
                ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
                //? 如何转化成一个User对象？
                User user=(User)objectInputStream.readObject();
                System.out.println(user);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                //TODO
            }
        }

        public static void main(String[] args) {
            new SocketServer().run();
        }

    }

}
