package chapter5;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class EchoSelectorProtocol implements TCPProtocol{
    //IO缓冲区的大小
    private int bufSize;

    public EchoSelectorProtocol(int bufSize) {
        this.bufSize = bufSize;
    }

    //ServerSocketChannel是注册的唯一支持"accept"方法的信道
    //当有客户端连接时,会创建一个SocketChannel
    //注册到Selector
    @Override
    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
        clntChan.configureBlocking(false);
        clntChan.register(key.selector(), SelectionKey.OP_ACCEPT, ByteBuffer.allocate(bufSize));
    }


    //读取信道数据
    @Override
    public void handleRead(SelectionKey key) throws IOException {
        SocketChannel clntChann = (SocketChannel) key.channel();
        //将数据读取到buf
        ByteBuffer buf =(ByteBuffer) key.attachment();
        long bytesRead = clntChann.read(buf);
        //已读完数据
        if(bytesRead == -1){
            clntChann.close();
        }
        //读取数据后允许数据读或写
        else if(bytesRead > 0){
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    @Override
    public void handleWrite(SelectionKey key) throws IOException {
        //key的附带物缓冲区
        ByteBuffer buf = (ByteBuffer) key.attachment();
        buf.flip();
        SocketChannel clntChan = (SocketChannel) key.channel();
        clntChan.write(buf);
        //读完缓冲区的数据后,说明可读
        if(!buf.hasRemaining()){
            key.interestOps(SelectionKey.OP_READ);
        }
        //缓冲区压缩更多空间
        buf.compact();
    }

    public static void main(String[] args) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putShort((short) 1);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putShort((short) 2);
        //array()返回后援数组
        byte[] array = bb.array();
        for (byte b : array) {
            System.out.println(b);
        }
    }
}
