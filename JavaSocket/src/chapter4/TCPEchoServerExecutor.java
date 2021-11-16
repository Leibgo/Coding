package chapter4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class TCPEchoServerExecutor {
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            throw new IllegalArgumentException("Parameter(s) : <Port>");
        }

        int echoServPort = Integer.parseInt(args[0]);
        ServerSocket servSock = new ServerSocket(echoServPort);
        Logger logger = Logger.getLogger("practical");
        //当有实现了Runnable接口的实例来调用execute方法时，总会尝试使用已有的线程
        //如果一个线程空闲了60秒以上,则将移除线程池
        /**
         * Executors是一个工厂类,产生各种类型的线程池来实现不同的调度策略
         * 例如本例中的根据负载情况自动调整大小的线程池
         * 也可以生成固定大小的线程池 newFixedThreadPool(threadPoolSize)
         * 或者只有一个线程的线程池  newSingleThreadExecutor()
         */
        ExecutorService service = Executors.newCachedThreadPool();  //缓存线程池服务
        while(true){
            Socket clntSock = servSock.accept();
            //调度策略：分配已有的线程或者创建一个新的线程
            service.execute(new EchoProtocol(clntSock, logger));
        }
    }
}
