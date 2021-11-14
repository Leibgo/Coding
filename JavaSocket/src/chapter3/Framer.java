package chapter3;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 定界符接口
 */
public interface Framer {
    void frameMsg(byte[] message, OutputStream out) throws IOException;
    byte[] nextMsg() throws IOException;
}
