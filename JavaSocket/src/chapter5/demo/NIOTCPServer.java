package chapter5.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * 服务器器端可以接收多个客户端的连接,Selector中可以有多个key
 */
public class NIOTCPServer {
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            throw new IllegalArgumentException("Parameter <Port>");
        }
        int port = Integer.parseInt(args[0]);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //绑定本地端口
        serverSocketChannel.bind(new InetSocketAddress(port));
        //channel设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        //注册serverSocketChannel的事件，accept()接收请求连接
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while(true){
            //所有通道中没有感兴趣的事件发生
            if(selector.select() == 0){
                continue;
            }
            //有事件发生
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                //表示处理该事件
                iterator.remove();
                //如果通道有连接请求发生
                if(key.isAcceptable()){
                    SocketChannel channel = serverSocketChannel.accept();
                    System.out.println("接受了来自:"+channel.socket().getInetAddress()+"的请求连接");
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    //向客户端传数据
                    String msg = "  hello world";
                    channel.write(ByteBuffer.wrap(msg.getBytes()));
                }
                //如果通道有数据
                if(key.isReadable()){
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    SocketChannel channel = (SocketChannel) key.channel();
                    buffer.clear();
                    int bytesRead = channel.read(buffer);
                    System.out.println(new String(buffer.array()));
                    //数据已全部读取完毕,关闭通道
                    if(bytesRead == -1){
                        channel.close();
                    }
                    //数据没有全部读取完
                    if(bytesRead > 0){
                        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    }
                }
//                //如果通道可以写数据
//                if(key.isWritable()){
//                    ByteBuffer buffer = (ByteBuffer) key.attachment();
//                    //TODO:出错点
//                    SocketChannel socket = (SocketChannel) key.channel();
//                    //因为无法保证能一次将buffer里的数据写进channel,因此while循环直到全部写完
//                    buffer.flip();
//                    while(buffer.hasRemaining()){
//                        socket.write(buffer);
//                    }
//                }
            }
        }
    }
}
