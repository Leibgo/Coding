package chapter2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class TCPEchoClient {
    public static void main(String[] args) throws IOException {
        //检测输入的参数个数的正确性 参数个数只能为2个或3个 （IP，数据，端口）|| (域名，数据，端口)
        if((args.length < 2) || (args.length > 3)){
            throw new IllegalArgumentException("Parameter(s): <Server> <Word> [<Port>]");
        }
        //服务器域名或IP
        String server = args[0];
        //将字符串转为字节数组
        byte[] data = args[1].getBytes();
        //服务器的端口
        int serverPort = (args.length == 3) ? Integer.parseInt(args[2]) : 7;
        //创建连接到特定服务器和端口的Socket
        Socket socket = new Socket(server, serverPort);
        System.out.println("Connect to Server ... sending echo string");
        //创建输入流、输出流
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        //发送数据
        out.write(data);
        //从服务器端接收到相同的数据
        int totalBytesRcvd = 0;
        int bytesRcvd;
        while (totalBytesRcvd < data.length){
            bytesRcvd = in.read(data, totalBytesRcvd, data.length - totalBytesRcvd);
            if (bytesRcvd == -1){
                throw new SocketException("Connection Closed Permanently");
            }
            totalBytesRcvd += bytesRcvd;
        }
        System.out.println("Received: " + new String(data));
        //关闭Socket
        socket.close();
    }
}
