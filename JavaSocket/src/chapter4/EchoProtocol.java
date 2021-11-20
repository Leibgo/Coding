package chapter4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EchoProtocol implements Runnable{
    //IO缓存
    private static final int BUFSIZE = 32;
    private final Socket clntSock;
    private final Logger logger;

    public EchoProtocol(Socket clntSock, Logger logger) {
        this.clntSock = clntSock;
        this.logger = logger;
    }

    //处理客户端请求的方法
    public static void handleEchoClient(Socket clntSock, Logger logger) {
        try {
            //获取输出流输入流
            InputStream in = clntSock.getInputStream();
            OutputStream out = clntSock.getOutputStream();
            //接收消息的大小
            int recvMsgSize;
            //已经接收到的字节
            int totalBytesEchoed = 0;
            byte[] echoBuffer = new byte[BUFSIZE];
            //接收消息并回显消息,直到关闭连接
            //in.read(echoBuffer):将输入流的消息读取到echoBuffer中
            //out.write(echoBuffer, 0 ,recvMsgSize):将缓冲数组中的数据写到输出流中
            while((recvMsgSize = in.read(echoBuffer)) != -1){
                out.write(echoBuffer, 0, recvMsgSize);
                totalBytesEchoed += recvMsgSize;
            }
            //打印日志
            logger.info("Client:" + clntSock.getRemoteSocketAddress() + ", echoed " +totalBytesEchoed + " bytes");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Exception in echo protocol", e);
        } finally {
            //关闭连接
            try {
                clntSock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        handleEchoClient(clntSock, logger);
    }
}
