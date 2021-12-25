package chapter2;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;

public class UDPEchoClientTimeout {
    //客户端等待响应的最长时间
    private static final int Timeout = 3000;
    //最大重试次数
    private static final int MAXTRIES = 5;

    public static void main(String[] args) throws IOException {
        if(args.length < 2 || args.length > 3){
            throw new IllegalArgumentException("Parameter(s): <Server> <Word> [<Port>]");
        }
        //服务器IP地址
        InetAddress serverAddress = InetAddress.getByName(args[0]);
        //发送的数据
        byte[] bytesToSend = args[1].getBytes();
        int servPort = (args.length == 3) ? Integer.parseInt(args[2]) : 7;
        //数据包套接字，不需要指定目的地址，即不需要连接，数据报文可以发送到任意的目的地
        DatagramSocket socket = new DatagramSocket();
        //设置等待时间，receive()方法的最长阻塞时间
        socket.setSoTimeout(Timeout);
        //创建发送的数据报文
        DatagramPacket sendPacket = new DatagramPacket(bytesToSend, bytesToSend.length, serverAddress, servPort);
        //创建接收的数据报文
        DatagramPacket recvPacket = new DatagramPacket(new byte[bytesToSend.length], bytesToSend.length);
        //尝试次数
        int tries = 0;
        //接收响应标志位
        boolean receivedResponse = false;
        while(!receivedResponse && tries < MAXTRIES){
            //发送数据
            socket.send(sendPacket);
            try{
                //接收报文
                socket.receive(recvPacket);
                //接收到了未知IP地址的数据
                if(!recvPacket.getAddress().equals(serverAddress)){
                    throw new IOException("Received packet from unknown source");
                }
                receivedResponse = true;
            }catch (InterruptedIOException e){
                tries++;
                System.out.println("Time out, " + (MAXTRIES - tries)+" more tries");
            }
        }
        if(receivedResponse){
            System.out.println("Received: " + new String(recvPacket.getData()));
        }else{
            System.out.println("No Response -- Giving Up");
        }
        socket.close();
    }
}
