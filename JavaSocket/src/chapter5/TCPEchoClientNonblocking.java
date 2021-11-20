package chapter5;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 非阻塞IO
 */
public class TCPEchoClientNonblocking {
    public static void main(String[] args) throws IOException {
        if(args.length < 2 || args.length > 3){
            throw new IllegalArgumentException("Paramter(s): <Server> <Word>");
        }
        String server = args[0];
        //使用默认的字符串将字符串转为字节数组
        byte[] argument = args[1].getBytes();
        //服务器端口
        int servPort = (args.length == 3) ? Integer.parseInt(args[2]) : 7;
        //创建channel并且设置为非阻塞
        SocketChannel clntChan = SocketChannel.open();
        clntChan.configureBlocking(false);
        //初始化到服务器的连接,重复的轮询IO源直到完成
        //在持续调用"finishConnect"来轮询连接状态
        if(!clntChan.connect(new InetSocketAddress(server, servPort))){
            while(!clntChan.finishConnect()) {
                System.out.println(".");
            }
        }
        //使用两种方法创建将要读写的ByteBuffer实例
        //1.包含了发送数据的byte[]数组
        //2.创建实例
        ByteBuffer writeBuf = ByteBuffer.wrap(argument);
        ByteBuffer readBuf = ByteBuffer.allocate(argument.length);
        //到目前为止接收到的字节数量
        int totalBytesRcvd = 0;
        //最后一次读取的字节数
        int bytesRcvd;
        while(totalBytesRcvd < argument.length){
            if(writeBuf.hasRemaining()){
                clntChan.write(writeBuf);
            }
            //返回0表示没有数据可读
            //返回-1表示连接提前关闭
            //对read()方法的调用不会造成阻塞，但是当没有数据可读时会返回0。
            if((bytesRcvd = clntChan.read(readBuf)) == -1){
                throw new SocketException("Connection Closed prematurely");
            }
            totalBytesRcvd += bytesRcvd;
            System.out.println(".");
        }
        System.out.println("Received: " + new String(readBuf.array(),0,totalBytesRcvd));
        clntChan.close();
    }
}

