package chapter2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPEchoServer {
    //数据包数据的最大容量，可容纳255个字节
    private static final int ECHOMAX = 255;

    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            throw new IllegalArgumentException("Parameters:<Port>");
        }
        //服务器端口
        int servPort = Integer.parseInt(args[0]);
        //建立Socket
        DatagramSocket socket = new DatagramSocket(servPort);
        //建立接收数据的数据报
        DatagramPacket packet = new DatagramPacket(new byte[ECHOMAX], ECHOMAX);
        //不断接收数据
        while(true){
            //接收到数据后，packet会包含客户端的IP和端口,作为自己的目标地址和端口号用来发送
            socket.receive(packet);
            System.out.println("正在处理客户端IP:" + packet.getAddress().getHostAddress() + " 端口:" + packet.getPort());
            socket.send(packet);
            //发送了数据包后，数据包的内部长度将被设置为刚处理过的长度，因此需要重新设置
            packet.setLength(ECHOMAX);
        }

    }
}
