package chapter5;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.sql.SQLOutput;
import java.util.Iterator;

public class TCPServerSelector {
    private static final int BUFSIZE = 256;
    private static final int TIMEOUT = 3000;

    public static void main(String[] args) throws IOException {
        if(args.length < 1){
            throw new IllegalArgumentException("Parameter(s): <Port> ...");
        }
        //创建选择器去多重监听套接字和连接信道
        Selector selector = Selector.open();
        for(String arg : args){
            ServerSocketChannel listnChannel = ServerSocketChannel.open();
            listnChannel.socket().bind(new InetSocketAddress(Integer.parseInt(arg)));
            listnChannel.configureBlocking(false);
            //将套接字channel注册到selector
            listnChannel.register(selector, SelectionKey.OP_ACCEPT);
        }

        //创建实现选择器协议的处理器
        TCPProtocol protocol = new EchoSelectorProtocol(BUFSIZE);
        //永远运行，处理有效的IO操作
        while(true){
            //查询在一组信道中，哪一个当前需要服务
            if(selector.select(TIMEOUT) == 0){
                System.out.println(".");
                continue;
            }
            //SelectorKey:Channel注册到Selector中的一个令牌
            //获取已经准备好IO通信的channel的selectionKey
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            while(keyIter.hasNext()){
                SelectionKey key = keyIter.next();
                //检测已经准备好的操作
                if(key.isAcceptable()){
                    protocol.handleAccept(key);
                }
                if(key.isReadable()){
                    protocol.handleRead(key);
                }
                if(key.isValid() && key.isWritable()){
                    protocol.handleWrite(key);
                }
                //移除令牌
                keyIter.remove();
            }
        }
    }
}
