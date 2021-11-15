package chapter3;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 定界符接口
 */
public interface Framer {
    /**
     * 写数据并在数据末尾添加定界符
     * @param message
     * @param out
     * @throws IOException
     */
    void frameMsg(byte[] message, OutputStream out) throws IOException;

    /**
     * 接收数据
     * @return
     * @throws IOException
     */
    byte[] nextMsg() throws IOException;
}
