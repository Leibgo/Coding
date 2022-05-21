# Hashmap

## 1. 数据结构

JDK1.7: 数组+链表

JDK1.8: 数组+链表+红黑树

每个元素都为`Node`节点

~~~java
static class Node<K,V> implements Map.Entry<K,V> {
    // 存放hash值    
    final int hash;
    // 存放key
    final K key;
    // 存放value
    V value;
    // 哈希冲突时使用链表地址法，连接下个节点
    Node<K,V> next;
}
~~~

当链表长度大于8时，链表会转换成红黑树，因为查询一个节点，树的时间复杂度是O(logn)，而链表是O(n)

## 2. 计算哈希值

总体分为三步：

1. 计算key.hashcode()
2. 高16位参与运算，减少哈希冲突 h = (key.hashcode()) ^ (h >> 16)
3. 与长度(n-1)进行&运算，index = h & (n-1) 得到桶的位置

~~~java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
~~~

问题：

为什么要让高16位参与运算？

> 当数组table长度很小时，例如table.length = 16，那么最终进行与运算时，将他转为二进制数是 0000000000001111
>
> 如果高位不参与运算，key的哈希值只有后四位才是有价值的。但需要知道，哈希值后四位为0的key是存在非常多的，这些key会被放到同一个桶
>
> 高位参与运算避免了大量的哈希冲突

为什么是&运算而不是取模运算？

> 因为table的长度是2的幂次方，因此对数字 h & (n-1)等价于 h % (n-1),但与运算比模运算速度来得快

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202204090955780.png" alt="img" style="zoom:80%;float:left" />

## 3. put()

![img](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202204091002899.png)

~~~java
public V put(K key, V value) {
 2     // 对key的hashCode()做hash
 3     return putVal(hash(key), key, value, false, true);
 4 }
 5 
 6 final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
 7                boolean evict) {
 8     Node<K,V>[] tab; Node<K,V> p; int n, i;
 9     // 步骤①：tab为空则创建
10     if ((tab = table) == null || (n = tab.length) == 0)
11         n = (tab = resize()).length;
12     // 步骤②：计算index，并对null做处理 
13     if ((p = tab[i = (n - 1) & hash]) == null) 
14         tab[i] = newNode(hash, key, value, null);
15     else {
16         Node<K,V> e; K k;
17         // 步骤③：节点key存在，直接覆盖value
18         if (p.hash == hash &&
19             ((k = p.key) == key || (key != null && key.equals(k))))
20             e = p;
21         // 步骤④：判断该链为红黑树
22         else if (p instanceof TreeNode)
23             e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
24         // 步骤⑤：该链为链表
25         else {
26             for (int binCount = 0; ; ++binCount) {
27                 if ((e = p.next) == null) {
28                     p.next = newNode(hash, key,value,null);
                        //链表长度大于8转换为红黑树进行处理
29                     if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st  
30                         treeifyBin(tab, hash);
31                     break;
32                 }
                    // key已经存在直接覆盖value
33                 if (e.hash == hash &&
34                     ((k = e.key) == key || (key != null && key.equals(k)))) 
35							break;
36                 p = e;
37             }
38         }
39         
40         if (e != null) { // existing mapping for key
41             V oldValue = e.value;
42             if (!onlyIfAbsent || oldValue == null)
43                 e.value = value;
44             afterNodeAccess(e);
45             return oldValue;
46         }
47     }

48     ++modCount;
49     // 步骤⑥：超过最大容量 就扩容
50     if (++size > threshold)
51         resize();
52     afterNodeInsertion(evict);
53     return null;
54 }
~~~

## 4. resize()

扩容(resize)就是重新计算容量，向HashMap对象里不停的添加元素，而HashMap对象内部的数组无法装载更多的元素时，对象就需要扩大数组的长度，以便能装入更多的元素。当然Java里的数组是无法自动扩容的，方法是使用一个新的数组代替已有的容量小的数组，就像我们用一个小桶装水，如果想装更多的水，就得换大水桶。

由于数组是2的幂次方扩展，因此节点在新数组的位置要么是老位置 j， 要么是 j + oldCap

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202204091008757.png" alt="img" style="zoom:80%;" />

元素在重新计算hash之后，因为n变为2倍，那么n-1的mask范围在高位多1bit(红色)，因此新的index就会发生这样的变化：

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202204091010428.png" alt="img" style="zoom:80%;float:left" />

因此，我们在扩充HashMap的时候，不需要像JDK1.7的实现那样重新计算hash，只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”，可以看看下图为16扩充为32的resize示意图：

![image-20220409102532654](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202204091025732.png)

~~~java
// 创建新的数组tab
Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
table = newTab;
if (oldTab != null) {
    // 遍历每个桶
    for (int j = 0; j < oldCap; ++j) {
        Node<K,V> e;
        if ((e = oldTab[j]) != null) {
            oldTab[j] = null;
            // 只有一个节点，直接放入新数组中
            if (e.next == null)
                newTab[e.hash & (newCap - 1)] = e;
            // 如果是红黑树,放入到红黑树的扩容中
            else if (e instanceof TreeNode)
                ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
            else { // preserve order
                // 双链表
                // low链表维护hash值新增的bit为0的节点
                // high链表维护hash值新增的bit为1的节点
                Node<K,V> loHead = null, loTail = null;
                Node<K,V> hiHead = null, hiTail = null;
                Node<K,V> next;
                do {
                    next = e.next;
                    if ((e.hash & oldCap) == 0) {
                        if (loTail == null)
                            loHead = e;
                        else
                            loTail.next = e;
                        loTail = e;
                    }
                    else {
                        if (hiTail == null)
                            hiHead = e;
                        else
                            hiTail.next = e;
                        hiTail = e;
                    }
                } while ((e = next) != null);
                // 将low链表放入newTab[j]
                if (loTail != null) {
                    loTail.next = null;
                    newTab[j] = loHead;
                }
                // 将high链表放入newTab[j+oldCap]
                if (hiTail != null) {
                    hiTail.next = null;
                    newTab[j + oldCap] = hiHead;
                }
            }
        }
    }
}
return newTab;
~~~

## 5. 线程不安全

### 5.1 线程不安全的来源

- 1.7版本时，扩容时采用头插法，多线程扩容时会发生循环链表或数据丢失
- 1.8版本时，尾插法解决了这个问题，但是仍线程不安全，会发生更新丢失的问题

JDK1.7 数组进行扩容时，会运行transfer方法进行数据的转移

~~~java
void transfer(Entry[] newTable, boolean rehash) {
    int newCapacity = newTable.length;
    for (Entry<K,V> e : table) {
        while(null != e) {
            Entry<K,V> next = e.next;
            if (rehash) {
                e.hash = null == e.key ? 0 : hash(e.key);
            }
            int i = indexFor(e.hash, newCapacity);
            e.next = newTable[i];
            newTable[i] = e;
            e = next;
        }
    }
}
~~~

JDK1.8 采用了尾插法，解决了循环链表的问题。但依然不是线程安全的，因为多线程在插入元素时，会发生更新丢失问题

采用了checkAndSet()，但没有保证原子性

```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    if ((p = tab[i = (n - 1) & hash]) == null)
        // a) 这行代码会发生线程安全问题
        tab[i] = newNode(hash, key, value, null);
	else{
        ...
    }
```

- A线程执行到a处代码发生线程切换
- 由于tab[i]还没有插入新节点，因此线程B也可以执行a)处代码，并插入自己的key-value节点
- A线程回来后，继续插入自己的key-value节点，将B的值覆盖

## 6. 其他

- 可以通过构造函数指定负载因子和初始容量，但初始化后就不可以变化了，final loadFactor
