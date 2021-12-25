package chapter3.example;

import chapter3.Framer;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class VoteClientUDP {
    public static void main(String[] args) throws IOException {
        if(args.length != 3){
            throw new IllegalArgumentException("Parameters: <Destination> <Port> <Candidate>");
        }

        InetAddress destAddress = InetAddress.getByName(args[0]);
        int destPort = Integer.parseInt(args[1]);
        int candidate = Integer.parseInt(args[2]);
        //UDP socket
        DatagramSocket socket = new DatagramSocket();
        socket.connect(destAddress, destPort);
        //创建投票
        VoteMsg vote = new VoteMsg(false, false, candidate, 0);
        //文本序列化工具
        VoteMsgCode coder = new VoteMsgTextCoder();
        //注意：UDP传输因为有消息边界,所以不需要帧
        byte[] encodedVote = coder.toWire(vote);
        System.out.println("Sending Text-Encoded Request (" + encodedVote.length + " bytes): ");
        System.out.println(vote);
        //发送请求
        DatagramPacket message = new DatagramPacket(encodedVote, encodedVote.length);
        socket.send(message);
        //接收响应
        message = new DatagramPacket(new byte[VoteMsgTextCoder.MAX_WIRE_LENGTH],VoteMsgTextCoder.MAX_WIRE_LENGTH);
        socket.receive(message);
        encodedVote = Arrays.copyOf(message.getData(), message.getLength());
        //解码
        vote = coder.fromWire(encodedVote);
        System.out.println(vote);
    }
}
