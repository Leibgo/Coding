package chapter3.example;

import chapter3.Framer;
import chapter3.LengthFramer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class VoteServerTCP {
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            throw new IllegalArgumentException("Parameter(s):<Port>");
        }
        //本地端口
        int port = Integer.parseInt(args[0]);
        //为服务器建立编码器和投票服务
        ServerSocket serverSocket = new ServerSocket(port);
        VoteMsgBinCoder coder = new VoteMsgBinCoder();
        VoteService service = new VoteService();

        while(true){
            //打印客户端地址
            Socket clntSocket = serverSocket.accept();
            System.out.println("收到来自:" + clntSocket.getRemoteSocketAddress() + "的请求");
            //为客户端创建成帧器
            Framer framer = new LengthFramer(clntSocket.getInputStream());
            try{
                byte[] req;
                while((req = framer.nextMsg()) != null){
                    //从客户端获取消息并解码
                    System.out.println("Received message (" + req.length + " bytes");
                    VoteMsg voteMsg = service.handleRequest(coder.fromWire(req));
                    //处理消息(加帧),并输出流发送响应
                    framer.frameMsg(coder.toWire(voteMsg), clntSocket.getOutputStream());
                }
            }catch (IOException e){
                System.err.println("Error handling client: "+ e.getMessage());
            } finally {
                System.out.println("Closing connection");
                clntSocket.close();
            }
        }
    }
}
