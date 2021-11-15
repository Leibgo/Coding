package chapter3.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class VoteServerUDP {
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            throw new IllegalArgumentException("Parameters: <Port>");
        }
        //服务器端口
        int port = Integer.parseInt(args[0]);
        //UDP socket
        DatagramSocket socket = new DatagramSocket(port);
        //创建缓存区
        byte[] inBuffer = new byte[VoteMsgTextCoder.MAX_WIRE_LENGTH];
        //序列化工具
        VoteMsgTextCoder coder = new VoteMsgTextCoder();
        //投票服务
        VoteService service = new VoteService();
        while(true){
            //数据报
            DatagramPacket packet = new DatagramPacket(inBuffer, inBuffer.length);
            //接收数据
            socket.receive(packet);
            //将接收到的数据存放到encodedMsg中
            byte[] encodedMsg = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            //字节数组->对象
            VoteMsg msg = coder.fromWire(encodedMsg);
            msg = service.handleRequest(msg);
            //发送数据 对象->字节数组
            packet.setData(coder.toWire(msg));
            System.out.println("Sending response...");
            socket.send(packet);
        }
    }
}
