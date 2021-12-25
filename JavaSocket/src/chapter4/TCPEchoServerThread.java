package chapter4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class TCPEchoServerThread {
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            throw new IllegalArgumentException("Parameter:<Port>");
        }
        //服务器监听的端口
        int echoServPort = Integer.parseInt(args[0]);
        //创建接收客户端连接请求的服务器socket
        ServerSocket servSock = new ServerSocket(echoServPort);
        Logger logger = Logger.getLogger("practical");
        //服务器Socket一直运行
        while(true){
            //在服务器端创建客户端socket
            Socket clntSock = servSock.accept();
            //为一个连接创建一个线程
            Thread thread = new Thread(new EchoProtocol(clntSock, logger));
            thread.start();
            logger.info("Created and started Thread " + thread.getName());
        }

    }
}
