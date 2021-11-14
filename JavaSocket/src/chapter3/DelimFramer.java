package chapter3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DelimFramer implements Framer{
    private InputStream in;
    //使用'\n'作为消息结束的定界符
    private static final byte DELIMITER = '\n';

    public DelimFramer(InputStream in) {
        this.in = in;
    }

    //写数据
    @Override
    public void frameMsg(byte[] message, OutputStream out) throws IOException {
        //检查消息中是否有定界符
        for (byte b : message) {
            if(b == DELIMITER){
                throw new IOException("消息中出现了定界符");
            }
        }
        out.write(message);
        //在消息的最后添加定界符
        out.write(DELIMITER);
        out.flush();
    }

    //从输入流中读取消息
    @Override
    public byte[] nextMsg() throws IOException {
        ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();
        int nextByte;
        //read():返回数据的下一个字节.当数据读取完毕,流要结束时返回-1
        while((nextByte = in.read()) != DELIMITER){
            if(nextByte == -1){
                //流中没有数据
                if(messageBuffer.size() == 0){
                    return null;
                }
                //缓存区中有数据但是没读到分界符,抛出异常
                else{
                    throw new IOException("消息没有定界符");
                }
            }
            //将数据写入缓存区
            messageBuffer.write(nextByte);
        }
        //将缓存区的数据以数组形式返回
        return messageBuffer.toByteArray();
    }
}
