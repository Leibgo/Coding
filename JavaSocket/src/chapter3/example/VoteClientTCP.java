package chapter3.example;

import chapter3.Framer;
import chapter3.LengthFramer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * TCP投票客户端
 */
public class VoteClientTCP {
    public static final int CANDIDATEID = 888;

    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            throw new IllegalArgumentException("Parameters(s):<Server> <Port>");
        }
        //服务器IP
        String destAddr = args[0];
        //服务器端口
        int destPort = Integer.parseInt(args[1]);
        //创建Sokcet
        Socket socket = new Socket(destAddr, destPort);
        OutputStream out = socket.getOutputStream();
        //构建二进制序列化工具类和基于长度的成帧器
        VoteMsgCode coder = new VoteMsgBinCoder();
        Framer framer = new LengthFramer(socket.getInputStream());
        //创建一个请求消息
        VoteMsg msg = new VoteMsg(false, true, CANDIDATEID, 0);
        byte[] encodedMsg = coder.toWire(msg);
        //发送请求
        System.out.println("Sending Inquiry (" + encodedMsg.length + ") bytes:");
        System.out.println(msg);
        framer.frameMsg(encodedMsg, out);
        //发送投票
        msg.setInquiry(false);
        encodedMsg = coder.toWire(msg);
        System.out.println("Sending Vote (" + encodedMsg.length + ") bytes:");
        framer.frameMsg(encodedMsg, out);
        //接收请求回复
        encodedMsg = framer.nextMsg();
        msg = coder.fromWire(encodedMsg);
        System.out.println("Receive Response (" + encodedMsg.length + " bytes");
        System.out.println(msg);
        //接收投票响应
        msg = coder.fromWire(framer.nextMsg());
        System.out.println("Received Response (" + encodedMsg.length + " bytes");
        System.out.println(msg);
        socket.close();
    }
}

