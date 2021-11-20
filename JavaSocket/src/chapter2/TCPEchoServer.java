package chapter2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class TCPEchoServer {
    //接收的缓冲区大小
    private static final int BUFFSIZE = 32;
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }
        //建立监听特定端口上的客户端连接请求的ServerSocket
        int serverPort = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(serverPort);

        int recvMsgSize;
        byte[] receiveBuffer = new byte[BUFFSIZE];
        //serverSocket永远都在运行，接收和服务请求
        while (true) {
            //ServerSocket的唯一目的，就是为新的TCP连接请求提供一个新的已连接的Socket实例
            Socket clntSocket = serverSocket.accept();
            //获取所连接的客户端IP+Port
            SocketAddress clientAddress = clntSocket.getRemoteSocketAddress();
            System.out.println("Handling client at " + clientAddress);
            //通过输入流输出流与远程客户端进行传输
            InputStream in = clntSocket.getInputStream();
            OutputStream out = clntSocket.getOutputStream();
            while((recvMsgSize = in.read(receiveBuffer)) != -1){
                out.write(receiveBuffer, 0, recvMsgSize);
            }
            //关闭Socket
            clntSocket.close();
        }
    }
}
