# 非阻塞与阻塞

在之前的Socket的学习中，使用的都是阻塞式IO。套接字的某些操作可能会无限期的阻塞。

例如在客户端，调用read方法可能会阻塞.

~~~java
InputStream sockIn = socket.getInputStream();
int byteRead;
byte[] buffer = new byte[BUFSIZE];
//当输入流没有数据时,read方法会阻塞
while((byteRead = sockIn.read(buffer)) != -1){
    fos.write(buffer, 0, byteRead);
    System.out.println("R");
}
~~~

在服务器端，调用accept方法也会阻塞

~~~java
//如果没有请求连接,将会一直阻塞
Socket clntSocket = serverSocket.accept();
~~~

**NIO 的强大功能部分来自于channel的非阻塞特性，通过配置它的阻塞行为，以实现非阻塞式的信道。**

~~~java
clntChan.configureBlocking(false);
~~~

通过非阻塞信道，这些可能会造成阻塞的方法调用都会立即返回。必须反复调用这些操作，直到所有I/O完成。

TCPEchoClientNonBlocking.java

在客户端操作中，可能会造成的阻塞是建立连接和读写。看看NIO是如何完成非阻塞的。

~~~java
//非阻塞操作
//1.通过持续调用finisConnect()方法来"轮询"连接状态,该方法在连接成功建立前一直返回false
if(!clntChan.connect(new InetSocketAddress(server, servPort))){
    while(!clntChan.finishConnect()) {
        System.out.println(".");
    }
}
//2.两个ByteBuffer实例,一个是写一个是读
ByteBuffer writeBuf = ByteBuffer.wrap(argument);
ByteBuffer readBuf = ByteBuffer.allocate(argument.length);
//到目前为止接收到的字节数量
int totalBytesRcvd = 0;
//最后一次读取的字节数
int bytesRcvd;
while(totalBytesRcvd < argument.length){
    if(writeBuf.hasRemaining()){
        clntChan.write(writeBuf);
    }
    //返回0表示没有数据可读
    //返回-1表示连接提前关闭
    //对read()方法的调用不会造成阻塞，但是当没有数据可读时会返回0。
    if((bytesRcvd = clntChan.read(readBuf)) == -1){
        throw new SocketException("Connection Closed prematurely");
    }
    totalBytesRcvd += bytesRcvd;
    System.out.println(".");
}
~~~

