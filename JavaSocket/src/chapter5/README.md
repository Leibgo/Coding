# NIO

## 1. 5种I/0模型

<img src="https://pic2.zhimg.com/v2-dc8a8a77294bd584a9e8ec1bd66cfa7e_r.jpg?source=1940ef5c" alt="preview" style="zoom:80%; float:left" />

- 同步阻塞

- 同步非阻塞

- 多路复用

- 信号驱动I/O

- 异步

  ### 同步和异步

  <font color=yellow>同步和异步关注的是消息通知的机制</font>

  同步：调用者等待被调用者完成后返回消息，才能继续执行

  异步：被调用者通过状态、通知、回调机制主动通知调用者的运行状态

  ###  阻塞和非阻塞

  <font color=yellow>阻塞和非阻塞关注的是(线程)等待消息通知时的状态</font>
  
  阻塞：blocking，指I/O操作需要彻底完成后才能返回到用户空间，调用结果返回之前，<font color=orange>调用者被挂起</font>
  
  非阻塞：nonblocking，指I/O操作被调用后立即返回一个状态给用户，无须等到I/O操作彻底结束，最终的结果返回之前，<font color=orange>调用者不会挂起</font>
  
  ### 比喻
  
  ~~~java
  以小明下载文件打个比方，从这两个关注点来再次说明这两组概念，希望能够更好的促进大家的理解。
  
  同步阻塞：小明一直盯着下载进度条，到 100% 的时候就完成。 - 同步体现在：等待下载完成通知。 - 阻塞体现在：等待下载完成通知过程中，不能做其他任务处理。
  
  同步非阻塞：小明提交下载任务后就去干别的，每过一段时间就去瞄一眼进度条，看到 100% 就完成。 - 同步体现在：等待下载完成通知。 - 非阻塞体现在：等待下载完成通知过程中，去干别的任务了，只是时不时会瞄一眼进度条。【小明必须要在两个任务间切换，关注下载进度】
  
  异步阻塞：小明换了个有下载完成通知功能的软件，下载完成就“叮”一声。不过小明不做别的事，仍然一直等待“叮”的声音。 - 异步体现在：下载完成“叮”一声通知。 - 阻塞体现在：等待下载完成“叮”一声通知过程中，不能做其他任务处理。
  
  异步非阻塞：仍然是那个会“叮”一声的下载软件，小明提交下载任务后就去干别的，听到“叮”的一声就知道完成了。 - 异步体现在：下载完成“叮”一声通知。 - 非阻塞体现在：等待下载完成“叮”一声通知过程中，去干别的任务了，只需要接收“叮”声通知即可。
      
  //同步/异步是“下载完成消息”通知的方式(机制)，而阻塞/非阻塞则是在等待“下载完成消息”通知过程中的状态,能不能干其他任务(策略)
  ~~~
  
  

### 1.1 同步阻塞型

<img src="https://static001.geekbang.org/infoq/89/8929de5d4f1c7744967d70ab1126b466.png" alt="img" style="zoom:70%;float:left" />

用户线程在内核进行I/O操作时被挂起。整个I/O请求中，用户线程是被阻塞的，这导致用户在I/O期间不能做任何事，CPU利用率不高。

### 1.2 同步非阻塞型

<img src="https://static001.geekbang.org/infoq/7a/7adcebe3b38cbbf256a77f274769151d.png" alt="img" style="zoom:80%;float:left" />

- 用户线程发起 IO 请求时立即返回。但并未读取到任何数据，用户线程需要不断地发起 IO 请求，直到数据到达后，才真正读取到数据，继续执行。即“轮询”机制
- 整个 IO 请求的过程中，虽然用户线程每次发起 IO 请求后可以立即返回，但是为了等到数据，仍需要不断地轮询、重复请求，消耗了大量的 CPU 的资源

### 1.3 IO多路复用

<img src="https://static001.geekbang.org/infoq/79/795cb6c7b7f5c3eab054a6857a7182bd.png" alt="img" style="zoom:80%;float:left" />

<font color=orange>多个连接会共用一个等待机制</font>，模型会阻塞在select或者poll两个系统调用上，而不是阻塞在真正的IO操作上。select 可以监控多个 IO 上是否已有 IO 操作准备就绪，即可达到在同一个线程内同时处理多个 IO 请求的目的。一旦内核发现select负责的一个或多个IO条件准备好时，就通知该进程。

用户首先将需要进行 IO 操作添加到 select 中，同时等待 select 系统调用返回。当数据到达时，IO 被激活，select 函数返回。用户线程正式发起 read 请求，读取数据并继续执行。

### 1.4 信号驱动

<img src="https://static001.geekbang.org/infoq/29/294db4d04c82f31388551ab310ea2257.png" alt="img" style="zoom:80%;float:left" />

用户进程可以通过 sigaction 系统调用注册一个信号处理程序，然后主程序可以继续向下执行，当有 IO 操作准备就绪时，由内核通知触发一个 SIGIO 信号处理程序执行，然后将用户进程所需要的数据从内核空间拷贝到用户空间
此模型的优势在于等待数据报到达期间进程不被阻塞。用户主程序可以继续执行，只要等待来自信号处理函数的通知

### 1.5 异步

<img src="https://static001.geekbang.org/infoq/60/60c3f1b47fd7fb91ff6a829fc54845aa.png" alt="img" style="zoom:80%;float:left" />

异步 IO 与信号驱动 IO 最主要的区别是信号驱动 IO 是由内核通知何时可以进行 IO 操作，而<font color=orange>异步 IO 则是由内核告诉用户线程 IO 操作何时完成。</font>信号驱动 IO 当内核通知触发信号处理程序时，信号处理程序还需要阻塞在从内核空间缓冲区复制数据到用户空间缓冲区这个阶段，而异步 IO 直接是在第二个阶段完成后，内核直接通知用户线程可以进行后续操作了

<img src="https://static001.geekbang.org/infoq/52/527999d677b74bb3ec786f569be56e43.png" alt="img" style="zoom:90%;float:left" />

## 2. 阻塞与非阻塞的demo

IO是面向流的处理，NIO是面向块(缓冲区)的处理

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

在客户端操作中，可能会造成的阻塞是建立连接和读写。看看NIO可以如何完成非阻塞的。

~~~java
//非阻塞操作
//1.通过持续调用finisConnect()方法来"轮询"连接状态,该方法在连接成功建立前一直返回false
//但这种处于忙等的状态
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

## 3. NIO的三大特性

### 3.1 NIO 三大核心部分组成

- Buffer：缓冲区
- Channel：信道
- Selector：选择器

### 3.2 Buffer

Buffer类维护了四大核心变量属性来提供关于数组的信息.

- 容量Capacity：**缓冲区能够容纳的数据元素的最大数量**。容量在缓冲区创建时被设定，并且永远不能被改变。(不能被改变的原因也很简单，底层是数组嘛)
- 上界Limit：**缓冲区里的数据的总数**，代表了当前缓冲区中一共有多少数据。
- 位置Position：**下一个要被读或写的元素的位置**。
- 标记Mark：一个备忘位置。**用于记录当前Position的位置**。通过reset()可以恢复

在使用缓冲区进行输入输出数据之前，必须确定缓冲区的postion、limit都已经正确设置了。

<font color=pink>缓存区的写模式：channel.read(buffer) 缓存区从信道读取数据并写入到缓存区</font>

<font color=pink>缓存区的读模式：channel.write(buffer) 信道从缓存区读取数据</font>

`flip()`：也称为"切换成读模式"，<font color=orange>对缓存区进行读取数据时，即channel要write(buffer)需要读取缓存区的数据前调用此方法</font>，将limit设置为当前的postion位置，将postion位置设置为0.

~~~java
public final Buffer flip() {
    limit = position;
    position = 0;
    mark = -1;
    return this;
}
~~~

`clear()`：将position设置为0，将limit设置为capacity，在channel.read(buffer)前调用此方法

~~~java
public final Buffer clear() {
    position = 0;
    limit = capacity;
    mark = -1;
    return this;l
}
~~~

`rewind() :` 将postion置为0

~~~java
public final Buffer rewind() {
    position = 0;
    mark = -1;
    return this;
}
~~~

`mark():` 将mark设置为position处

~~~java
public final Buffer mark() {
    mark = position;
    return this;
}
~~~

`reset():`将postion充置为上次mark处

~~~java
public final Buffer reset() {
    int m = mark;
    if (m < 0)
        throw new InvalidMarkException();
    position = m;
    return this;
}
~~~

`compact()`：可以理解为"切换成写模式"，将当前postion和limit间的数据复制到缓冲区的开始位置，从而为<font color='orange'>缓存区的写模式腾出空间</font>。position设置为复制数据的长度，limit设置为capacity，这样写入的数据就不会覆盖之前未读的数据。

~~~java
//compact()方法将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面
~~~

<img src="https://itqiankun.oss-cn-beijing.aliyuncs.com/picture/blogArticles/2020-04-29/1588158646.png" alt="img" style="zoom:55%;float:left" />

### 3.3 Channel

<img src="https://itqiankun.oss-cn-beijing.aliyuncs.com/picture/blogArticles/2020-04-29/1588158667.png" alt="img" style="zoom:60%;float:left" />

Channel通道<font color=orange>只负责传输数据、不直接操作数据的</font>。操作数据都是通过Buffer缓冲区来进行操作！Channel与前面介绍的 Buffer 打交道，读操作的时候将 Channel 中的数据填充到 Buffer 中，而写操作时将 Buffer 中的数据写入到 Channel 中。

<img src="https://itqiankun.oss-cn-beijing.aliyuncs.com/picture/blogArticles/2020-04-29/1588158677.png" alt="img" style="zoom:80%;float:left" />

- FileChannel：文件通道，用于文件的读和写
- DatagramChannel：用于 UDP 连接的接收和发送
- SocketChannel：把它理解为 TCP 连接通道，简单理解就是 TCP 客户端
- ServerSocketChannel：TCP 对应的服务端，用于监听某个端口进来的请求

### 3.4 Selector

Selector 建立在非阻塞的基础之上，大家经常听到的 <font color=orange>多路复用</font> 在 Java 世界中指的就是它，<font color=orange>用于实现一个线程管理多个 Channel。</font>

多路复用的核心在于使用一个 Selector 来管理多个通道，可以是 SocketChannel，也可以是 ServerSocketChannel，将各个通道注册到 Selector 上，指定监听的事件；

之后可以只用一个线程来轮询这个 Selector，看看上面是否有通道是准备好的，当通道准备好可读或可写，然后才去开始真正的读写，这样就避免了给每个通道都开启一个线程。

每一个Selector都有三个键集，<font color=pink>Set<SelectionKey> keys</font>，<font color=pink>Set<SelectionKey> selectedKeys</font>，<font color=pink>Set<SelectionKey> cancelKeys</font>

注册后事件存在于 keys 中,经过 select() 后，系统发现有事件准备好了，该事件的key就会被选择，加入selectedKeys中，select(）返回值为已准备好的key数量，该方法为阻塞方法，只有返回值为大于0才返回（经过很长一段时间后无果也会返回），可以被wakeUp()方法唤醒

<font color=yellow>1.开启Selector</font>

~~~java
Selector selector = Selector.open();
~~~

<font color=yellow>2.将channel注册到Selector中</font>

~~~java
// 将通道设置为非阻塞模式，因为默认都是阻塞模式的
channel.configureBlocking(false);
// 注册
SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
~~~

register 方法的第二个 int 型参数（使用二进制的标记位）用于表明需要监听哪些感兴趣的事件。例如服务器需要监听接收TCP连接请求、读写数据等事件。

~~~java
SelectionKey.OP_READ //对应 00000001，通道中有数据可以进行读取的事件

SelectionKey.OP_WRITE //对应 00000100，可以往通道中写入数据的事件

SelectionKey.OP_CONNECT //对应 00001000，成功建立 TCP 连接的事件

SelectionKey.OP_ACCEPT //对应 00010000，接受 TCP 连接的事件
~~~



<font color=yellow>3.简单示例</font>

~~~java
Selector selector = Selector.open();
channel.configureBlocking(false);
SelectionKey key = channel.register(selector, SelectionKey.OP_ACCEPT);
while(true) {
  // 轮训地获取选择器上已“就绪”的通道事件--->只要select()>0，说明事件已就绪
  int readyChannels = selector.select();
  if(readyChannels == 0) continue;
  // 遍历
  Set<SelectionKey> selectedKeys = selector.selectedKeys();
  Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
  while(keyIterator.hasNext()) {
    SelectionKey key = keyIterator.next();
    if(key.isAcceptable()) {
        // a connection was accepted by a ServerSocketChannel.
    } else if (key.isConnectable()) {
        // a connection was established with a remote server.
    } else if (key.isReadable()) {
        // a channel is ready for reading
    } else if (key.isWritable()) {
        // a channel is ready for writing
    }
    keyIterator.remove();
  }
}
~~~

