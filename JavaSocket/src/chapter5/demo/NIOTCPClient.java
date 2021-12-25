package chapter5.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 客户端对应一个Socket -> SocketChannel
 */
public class NIOTCPClient {
    public static void main(String[] args) throws IOException {
        if(args.length < 2 || args.length > 3){
            throw new IllegalArgumentException("Parameters:<IP> <Data> <Port>");
        }
        String server = args[0];
        byte[] data = args[1].getBytes();
        int port = args.length == 3 ? Integer.parseInt(args[2]) : 7;
        //服务器端ip+端口号
        InetSocketAddress serverAddress = new InetSocketAddress(server, port);
        //客户端socketchannel
        SocketChannel clientSocketChannel = SocketChannel.open();
        //客户端Selector
        Selector selector = Selector.open();
        //设置为非阻塞
        clientSocketChannel.configureBlocking(false);
        //connect方法为非阻塞方法,返回时可能未连接成功
        clientSocketChannel.connect(serverAddress);
        clientSocketChannel.register(selector, SelectionKey.OP_READ);
        //保证客户端能成功连接
        while(!clientSocketChannel.finishConnect()){
            System.out.println("客户端正在连接中...." + serverAddress);
        }
        //查看是否有客户端selector的通道感兴趣事件发生
        while(true){
            if(selector.select() == 0){
                continue;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                if(key.isReadable()){
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    clientSocketChannel.read(buffer);
                    System.out.println("接收到来自服务器的数据:" + new String(buffer.array()));
                    key.interestOps(SelectionKey.OP_WRITE);
                    String msg = " 我已连接上服务器";
                    clientSocketChannel.write(ByteBuffer.wrap(msg.getBytes()));
                }
//                if(key.isWritable()){
//                    ByteBuffer attachment = (ByteBuffer) key.attachment();
//                    attachment.flip();
//                    while(attachment.hasRemaining()){
//                        clientSocketChannel.write(attachment);
//                    }
//                }
                iterator.remove();
            }

        }
    }
}
