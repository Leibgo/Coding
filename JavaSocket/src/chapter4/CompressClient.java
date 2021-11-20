package chapter4;

import java.io.*;
import java.net.Socket;

public class CompressClient {
    public static final int BUFSIZE = 256;

    public static void main(String[] args) throws IOException {
        if(args.length != 3){
            throw new IllegalArgumentException("Parameter<s>: <Server> <Port> <File>");
        }
        //服务器端IP地址
        String server = args[0];
        int port = Integer.parseInt(args[1]);
        String fileName = args[2];
        //打开输入和输出文件
        FileInputStream fis = new FileInputStream(fileName);
        FileOutputStream fos = new FileOutputStream(fileName + ".gz");
        //创建Socket
        Socket socket = new Socket(server, port);
        //发送未压缩的文件到服务器
        sendBytes(socket, fis);

        //接收压缩的文件
        InputStream sockIn = socket.getInputStream();
        int byteRead;
        byte[] buffer = new byte[BUFSIZE];
        while((byteRead = sockIn.read(buffer)) != -1){
            fos.write(buffer, 0, byteRead);
            System.out.println("R");
        }
        //关闭socket和文件输入输出流
        socket.close();
        fis.close();
        fos.close();
    }

    private static void sendBytes(Socket socket, FileInputStream fis) throws IOException {
        OutputStream os = socket.getOutputStream();
        int bytesRead;
        byte[] buffer = new byte[BUFSIZE];
        while((bytesRead = fis.read(buffer)) != -1){
            os.write(buffer, 0, bytesRead);
            System.out.println("W");
        }
        //关闭socket的输出流，表明数据已经传送完毕
        socket.shutdownOutput();

    }
}
