package chapter3;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class LengthFramer implements Framer{
    public static final int MAXMESSAGELENGTH = 65535;
    public static final int BYTEMASK = 0xff;
    public static final int SHORTMASK = 0xffff;
    public static final int BYTESHIFT = 8;

    private DataInputStream in;

    public LengthFramer(DataInputStream in) {
        this.in = in;
    }

    @Override
    public void frameMsg(byte[] message, OutputStream out) throws IOException {
        if(message.length > MAXMESSAGELENGTH){
            throw new IOException("消息太长了");
        }
        //在消息前写入数据长度
        out.write((message.length >> BYTESHIFT) & BYTEMASK);
        out.write(message.length & BYTEMASK);
        //输出数据
        out.write(message);
        out.flush();
    }

    @Override
    public byte[] nextMsg() throws IOException {
        int length;
        try{
            //读取前两个字节
            length = in.readUnsignedShort();
        }
        //流中只有0个或1个字节
        catch (EOFException e){
            return null;
        }
        // 0 <= length <= 65535
        byte[] msg = new byte[length];
        //readFully将阻塞等待，直到接收到了足够多的字节填满指定的数组
        in.readFully(msg);
        return msg;
    }
}
