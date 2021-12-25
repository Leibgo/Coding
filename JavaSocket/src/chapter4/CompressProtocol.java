package chapter4;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressProtocol implements Runnable{
    public static final int BUFSIZE = 1024;
    private Socket clntSocket;
    private Logger logger;

    public CompressProtocol(Socket clntSocket, Logger logger) {
        this.clntSocket = clntSocket;
        this.logger = logger;
    }
    //压缩文件
    public static void handlerCompress(Socket clntSocket, Logger logger){
        try {
            InputStream in = clntSocket.getInputStream();
            GZIPOutputStream GzipOut = new GZIPOutputStream(clntSocket.getOutputStream());
            byte[] buffer = new byte[BUFSIZE];
            int bytesRead;
            while((bytesRead = in.read(buffer)) != -1){
                GzipOut.write(buffer, 0, bytesRead);
            }
            GzipOut.finish();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Exception in echo protocol", e);
        }

        try {
            clntSocket.close();
        } catch (IOException e) {
            logger.info("Exception = " + e.getMessage());
        }
    }
    public void run() {
        handlerCompress(this.clntSocket, this.logger);
    }
}
