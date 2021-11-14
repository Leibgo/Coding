## 知识点罗列
1.部分outputstream的子类(例如BufferedOutputstream)实现了缓存机制,为了提高效率当write()的时候不一定直接发过去,
有可能先缓存起来一起发.flush()的作用就是强制性地将缓存中的数据发出去.

2.