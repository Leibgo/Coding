## 知识点罗列
1.部分outputstream的子类(例如BufferedOutputstream)实现了缓存机制,为了提高效率当write()的时候不一定直接发过去,
有可能先缓存起来一起发.flush()的作用就是强制性地将缓存中的数据发出去.

2.帧：定位消息的结束位置.使用TCP套接字时,成帧是一个非常重要的考虑因素,因为TCP协议
中没有消息边界的概念,无法确定消息的长度和结束位置.
主要两种技术找到消息的结束位置
- 基于定界符(Delimiter-based) 消息的结束由一个"唯一的标记"指出,发送者在传输完数据后显式添加特殊的字节序列
- 显示长度(Explict-length) 在消息前加一个固定大小的字段,指示消息包含了多少字节

3.数据的传输格式变化

TCP: 对象 -》 字节数组 -》 "加帧"  .... "去帧" -》 字节数组 -》 对象

UDP: 对象 -》 字节数组 ... 字节数组 -》 对象