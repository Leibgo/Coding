package chapter4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadEchoServerPool {
    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            throw new IllegalArgumentException("Parameter(s): <Port> <Threads>");
        }
        //监听端口
        int echoServPort = Integer.parseInt(args[0]);
        //线程数
        int threadPoolSize = Integer.parseInt(args[1]);
        ServerSocket servSock = new ServerSocket(echoServPort);
        Logger logger = Logger.getLogger("paractical");
        //线程池服务客户端请求
        for(int i = 0; i < threadPoolSize; i++){
            //最多有threadPoolSize个线程会阻塞在accept
            Thread thread = new Thread(()->{
                while(true){
                    try {
                        //等待连接
                        Socket clntSock = servSock.accept();
                        EchoProtocol.handleEchoClient(clntSock, logger);
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Client accept failed", e);
                    }
                }
            });
            thread.start();
            logger.info("Created and started Thread = " + thread.getName());
        }
    }
}
