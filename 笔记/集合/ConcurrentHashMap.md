

# ConcurrentHashMap

> 基于JDK1.8

## 1. 初始化

- 未指定容量初始化，ConcurrentHashMap<String, Object> map = new ConcurrentHashMap()，不会做任何处理，在put时初始化为容量 = 16的table
- 指定容量初始化，ConcurrentHashMap<String, Object> map = new ConcurrentHashMap(n)，容量会进行处理 n -> n'，变成比n'大的最小的2次幂数。例如32，处理后变为64

<font color=orange>初始化函数不会对table进行初始化，table的初始化在第一次put时进行，延迟初始化</font>

~~~java
public ConcurrentHashMap(int initialCapacity) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException();
    int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
               MAXIMUM_CAPACITY :
               // 比tableSizeFor(n):获取比n大的最小2次幂数最为初始容量
               tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
    this.sizeCtl = cap;
}
~~~

## 2. sizeCtl

构造方法里都会涉及到sizeCtl，这个变量非常重要，不同的值对应的含义也不一样，这里我们先对这个变量不同的值的含义做一下说明

`sizeCtl`值为0，代表数组还未初始化，且数组的初始容量为16

`sizeCtl`值为整数，如果数组还未初始化，那么记录的是数组的初始容量，如果数组已经初始化，那么记录数组的扩容阈值(数组的初始容量*0.75）

`sizeCtl`为-1，表示数组正在进行初始化

`sizeCtl`小于0，并且不是-1，表示数组正在进行扩容，-(n+1)表示此时有n个线程正在共同完成数组的扩容操作，高16位表示扩容表示戳，低16位表示参与扩容的线程数量+1

## 3. 数组的初始化

```java
private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    while ((tab = table) == null || tab.length == 0) {
        // 数组正在被初始化，当前线程让出cpu的执行权
        if ((sc = sizeCtl) < 0)
            Thread.yield(); // lost initialization race; just spin
        // cas将sizeCtl置为-1，当前线程进行初始化
        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            try {
                if ((tab = table) == null || tab.length == 0) {
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                    @SuppressWarnings("unchecked")
                    // 数组初始化
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    table = tab = nt;
                    // sc = 0.75n，sc被设置为扩容阈值
                    sc = n - (n >>> 2);
                }
            } finally {
                // 赋值给sizeCtl
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
}
```

## 4. 添加元素

~~~java
public V put(K key, V value) {
    return putVal(key, value, false);
}
~~~

```java
final V putVal(K key, V value, boolean onlyIfAbsent) {
    // 不允许key、value为空
    if (key == null || value == null) throw new NullPointerException();
    // 基于key计算hash值，进行扰动处理(高16位参与运算)，同时确保hash值 > 0
    // 后面利用hash值判断节点的类型，并且确保了哈希值是整数
    int hash = spread(key.hashCode());
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        if (tab == null || (n = tab.length) == 0)
            tab = initTable();
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value, null)))
                break;                   // no lock when adding to empty bin
        }
        // 数组正在扩容
        else if ((fh = f.hash) == MOVED)
            tab = helpTransfer(tab, f);
        else {
            V oldVal = null;
            // 锁住头节点
            synchronized (f) {
                if (tabAt(tab, i) == f) {
                    // 哈希值大于0，表示是链表结构
                    if (fh >= 0) {
                        binCount = 1;
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    // 哈希值小于0，表示是树的结构
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                       value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            if (binCount != 0) {
                // 链表长度大于64，进行红黑树的转换
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    // 最后一个函数的作用是查看添加元素后，元素个数大小是否超过了阈值，超过则扩容
    addCount(1L, binCount);
    return null;
}
```

在计算哈希值时，ConcurrentHashMap必须确保它的哈希值大于>=0，spread(hashcode)做这件事

当哈希值小于0时：

```java
static final int MOVED     = -1; // hash for forwarding nodes
static final int TREEBIN   = -2; // hash for roots of trees
```

- -1：表示当前节点已经被迁移，并且对象正在进行扩容，当前线程去协助扩容
- -2：表示当前节点是红黑树节点

## 5. 维护元素个数

> 在hashmap中，增加元素的个数并检查是否超过扩容阈值是非常简单的
>
> ```java
> if (++size > threshold)
>     resize();
> ```
>
> 但这样是线程不安全的，++size不是原子性，多线程情况下，会发生更新丢失的问题

- 在JDK8的实现中使用了CounterCell+baseCount来辅助统计元素的个数。baseCount是ConcurrentHashMap的一个属性，某个线程在调用put方法时，会先通过CAS修改baseCount的值，如果CAS成功，就计数成功。如果CAS失败，则会从CountCell中随机选出一个CounterCell对象，然后利用CAS去修改CounterCell对象中的值。

- 所以在统计最终的元素个数时，baseCount加上所有CountCell中的value值，所得的和就是所有的元素个数

### 1. addCount

```java
private final void addCount(long x, int check) {
    CounterCell[] as; long b, s;
    // CAS修改baseCount
    if ((as = counterCells) != null || !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
        // 1.as != null
        // 2.CAS失败
        CounterCell a; long v; int m;
        boolean uncontended = true;
        // as为null
        if (as == null || (m = as.length - 1) < 0 || (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
            !(uncontended = U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
            // 调用fullAddCount函数
            fullAddCount(x, uncontended);
            return;
        }
        if (check <= 1)
            return;
        // 计算元素个数
        s = sumCount();
    }
    // check为put时传入的链表长度个数
    if (check >= 0) {
        Node<K,V>[] tab, nt; 
        int n, sc;
        // 元素个数大于扩容阈值进行扩容
        while (s >= (long)(sc = sizeCtl) && (tab = table) != null && (n = tab.length) < MAXIMUM_CAPACITY) {
            int rs = resizeStamp(n);
            if (sc < 0) {
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                    transferIndex <= 0)
                    break;
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    transfer(tab, nt);
            }
            else if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2))
                transfer(tab, null);
            s = sumCount();
        }
    }
}
```

### 2. fullAddCount

<font color=orange>随机选择一个CountCell，更新它的value</font>

```java
private final void fullAddCount(long x, boolean wasUncontended) {
    int h;
    // 随机获得CountCell的一个小标
    if ((h = ThreadLocalRandom.getProbe()) == 0) {
        ThreadLocalRandom.localInit();     
        h = ThreadLocalRandom.getProbe();
        wasUncontended = true;
    }
    boolean collide = false;                // True if last slot nonempty
    for (;;) {
        CounterCell[] as; CounterCell a; int n; long v;
        // as已经初始化完毕
        if ((as = counterCells) != null && (n = as.length) > 0) {
           	// countcell === null
            if ((a = as[(n - 1) & h]) == null) {
                if (cellsBusy == 0) {            // Try to attach new Cell
                    CounterCell r = new CounterCell(x); // Optimistic create
                    // CAS 设置 cellbusy
                    if (cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                        boolean created = false;
                        try {               
                            CounterCell[] rs; int m, j;
                            // 将新创建的CountCell对象设置进CountCell数组
                            if ((rs = counterCells) != null && (m = rs.length) > 0 && rs[j = (m - 1) & h] == null) {
                                rs[j] = r;
                                created = true;
                            }
                        } finally {
                            cellsBusy = 0;
                        }
                        if (created)
                            break;
                        continue;           // Slot is now non-empty
                    }
                }
                collide = false;
            }
            else if (!wasUncontended)       // CAS already known to fail
                wasUncontended = true;      // Continue after rehash
            // CAS更新CounterCell的值，成功就结束循环
            else if (U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))
                break;
            else if (counterCells != as || n >= NCPU)
                collide = false;            // At max size or stale
            else if (!collide)
                collide = true;
            // 进行扩容
            else if (cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                try {
                    // CountCell数组扩大两倍
                    if (counterCells == as) {// Expand table unless stale
                        CounterCell[] rs = new CounterCell[n << 1];
                        for (int i = 0; i < n; ++i)
                            rs[i] = as[i];
                        counterCells = rs;
                    }
                } finally {
                    cellsBusy = 0;
                }
                collide = false;
                continue;                   // Retry with expanded table
            }
            // 执行到这一步，说明计数失败，重新计算hash挑选新的CountCell
            h = ThreadLocalRandom.advanceProbe(h);
        }
        // CountCell为null，进行初始化扩容
        else if (cellsBusy == 0 && counterCells == as && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
            boolean init = false;
            try {                           // Initialize table
                if (counterCells == as) {
                    CounterCell[] rs = new CounterCell[2];
                    // 新创建CounterCell对象
                    rs[h & 1] = new CounterCell(x);
                    counterCells = rs;
                    init = true;
                }
            } finally {
                cellsBusy = 0;
            }
            if (init)
                break;
        }
        else if (U.compareAndSwapLong(this, BASECOUNT, v = baseCount, v + x))
            break;                          // Fall back on using base
    }
}
```

### 3. sumCount

<font color=orange>计算元素个数总和：CountCell + baseCount;</font>

~~~java
final long sumCount() {
    CounterCell[] as = counterCells; CounterCell a;
    long sum = baseCount;
    if (as != null) {
        for (int i = 0; i < as.length; ++i) {
            if ((a = as[i]) != null)
                sum += a.value;
        }
    }
    return sum;
}
~~~

## 6. 扩容

协助扩容发生在两个节点：

1. 添加元素时，发现添加位置的节点 node.hash = MOVED，协助扩容
2. 计算元素个数时，发现sizeCtl < 0，协助扩容

### 1. addCount

~~~java
private final void addCount(long x, int check) {
    ... s = 元素个数 
    // check为put时传入的链表长度个数
    if (check >= 0) {
        Node<K,V>[] tab, nt; 
        int n, sc;
        // 元素个数大于扩容阈值进行扩容
        while (s >= (long)(sc = sizeCtl) && (tab = table) != null && (n = tab.length) < MAXIMUM_CAPACITY) {
            // resizeStamp的作用是将n这个值左移15位
            int rs = resizeStamp(n);
            // sc < 0,说明有别的线程在进行扩容，当前线程协助扩容
            if (sc < 0) {
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 || sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                    transferIndex <= 0)
                    break;
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    // 协助扩容
                    transfer(tab, nt);
            }
            // sc > 0,则表示元素个数大于扩容阈值，需要进行扩容
            // 将sizeCtl设置为小于0的负数，且不为-1，表示正在进行扩容
            else if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2))
                // 进行扩容
                transfer(tab, null);
            s = sumCount();
        }
    }
}
~~~

### 2. transfer

多个线程共同扩容

~~~java
private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
    int n = tab.length, stride;
    // CPU个数是否大于1 ？大于1，则stride = (n >>> 3) / NCPU ： 否则意味着cpu个数只有1，最多也只有一个线程并发，那么有这个线程负责所有的旧数据扩容
    // stride 小于 MIN_TRANSFER_STRIDE = 16，则将stride设置为16
    if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
        stride = MIN_TRANSFER_STRIDE; // subdivide range
    // 初始化扩容:一个线程在进行扩容
    if (nextTab == null) {            // initiating
        try {
            @SuppressWarnings("unchecked")
            // 创建新的数组（假设容量为32，那么现在新的数组容量变成64）
            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
            nextTab = nt;
        } catch (Throwable ex) {      // try to cope with OOME
            sizeCtl = Integer.MAX_VALUE;
            return;
        }
        // nextTable便是新的数组
        nextTable = nextTab;
        // 当前线程开始转移的起始位置=旧数组的size(从后往前开始转移)
        transferIndex = n;
    }
    int nextn = nextTab.length;
    ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
    boolean advance = true;
    boolean finishing = false; // to ensure sweep before committing nextTab
    for (int i = 0, bound = 0;;) {
        Node<K,V> f; int fh;
        // 给每个线程划定它负责的数据区域
        while (advance) {
            int nextIndex, nextBound;
            if (--i >= bound || finishing)
                advance = false;
            else if ((nextIndex = transferIndex) <= 0) {
                i = -1;
                advance = false;
            }
            // 下一个线程的transferIndex = 设为48
            // bound = 48
            // i = nextIndex - 1 = 63
            else if (U.compareAndSwapInt(this, TRANSFERINDEX, nextIndex,nextBound = (nextIndex > stride ? nextIndex - stride : 0))) {
                bound = nextBound;
                i = nextIndex - 1;
                advance = false;
            }
        }
        //
        if (i < 0 || i >= n || i + n >= nextn) {
            int sc;
            // 当前线程已经完成数据迁移，
            if (finishing) {
                nextTable = null;
                table = nextTab;
                // sizeCtl = 2n - 1/2n = 3/2n = 0.75 * 2n
                sizeCtl = (n << 1) - (n >>> 1);
                return;
            }
            // 完成扩容任务后的线程，将sizeCtl-1，维护着sizeCtrl的低16位
            if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                // 这步代表查看当前线程是否是扩容的最后一个线程
                if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                    return;
                // 如果是，则finishing=true
                finishing = advance = true;
                // 重新检查老表，查看有没有遗落的slot
                i = n; 
            }
        }
        else if ((f = tabAt(tab, i)) == null)
            advance = casTabAt(tab, i, null, fwd);
        else if ((fh = f.hash) == MOVED)
            advance = true; 
        // 进行数据转移
        else {
            // 加锁，不允许迁移的时候其他线程对这个链表和红黑树添加元素
            synchronized (f) {
                // 链表的扩容
                if (tabAt(tab, i) == f) {
                    Node<K,V> ln, hn;
                    if (fh >= 0) {
                        // 查看新增的那位bit是1还是0
                        int runBit = fh & n;
                        // 最后一个节点
                        Node<K,V> lastRun = f;
                        // 这个for循环查出最后的lastRun节点和runBit节点
                        for (Node<K,V> p = f.next; p != null; p = p.next) {
                            int b = p.hash & n;
                            if (b != runBit) {
                                runBit = b;
                                lastRun = p;
                            }
                        }
                        // runBit=0，
                        if (runBit == 0) {
                            ln = lastRun;
                            hn = null;
                        }
                        else {
                            hn = lastRun;
                            ln = null;
                        }
                        // 使用头插法插入节点
                        for (Node<K,V> p = f; p != lastRun; p = p.next) {
                            int ph = p.hash; K pk = p.key; V pv = p.val;
                            if ((ph & n) == 0)
                                ln = new Node<K,V>(ph, pk, pv, ln);
                            else
                                hn = new Node<K,V>(ph, pk, pv, hn);
                        }
                        // 将低位的链表放在i位置不动
                        setTabAt(nextTab, i, ln);
                        // 将高位的链表放在(i+n)位置上
                        setTabAt(nextTab, i + n, hn);
                        // 迁移完成后的位置都变成了fwd，表示已经迁移
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }
                    // 二叉树的扩容
                    else if (f instanceof TreeBin) {
                        TreeBin<K,V> t = (TreeBin<K,V>)f;
                        TreeNode<K,V> lo = null, loTail = null;
                        TreeNode<K,V> hi = null, hiTail = null;
                        int lc = 0, hc = 0;
                        for (Node<K,V> e = t.first; e != null; e = e.next) {
                            int h = e.hash;
                            TreeNode<K,V> p = new TreeNode<K,V>
                                (h, e.key, e.val, null, null);
                            if ((h & n) == 0) {
                                if ((p.prev = loTail) == null)
                                    lo = p;
                                else
                                    loTail.next = p;
                                loTail = p;
                                ++lc;
                            }
                            else {
                                if ((p.prev = hiTail) == null)
                                    hi = p;
                                else
                                    hiTail.next = p;
                                hiTail = p;
                                ++hc;
                            }
                        }
                        ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                        (hc != 0) ? new TreeBin<K,V>(lo) : t;
                        hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                        (lc != 0) ? new TreeBin<K,V>(hi) : t;
                        setTabAt(nextTab, i, ln);
                        setTabAt(nextTab, i + n, hn);
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }
                }
            }
        }
    }
}
~~~

### 3. 扩容标识戳

多线程同时对一个数组进行扩容时，如果oldTab的长度相同，那么sizeCtl的高16位，即扩容表示戳也是相同的

具体的计算如下：

~~~java

private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
private static int RESIZE_STAMP_BITS = 16;

int rs = resizeStamp(n);
// 将扩容表示戳位于高位16
// +2表示当前线程是触发扩容的线程，我们知道sizeCtl的低16位是线程数+1，那么触发扩容时，1+1=2;
U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2)

// 计算扩容表示戳
static final int resizeStamp(int n) {
   // 将计算出来的0个数与1000000000000000进行或运行，为什么是这个数，是为了让sizeCtl小于0
   return Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1));
}

// 计算第一个非0位前有多少个0位
public static int numberOfLeadingZeros(int i) {
    // HD, Figure 5-6
    if (i == 0)
        return 32;
    // n预先已设置为1
    int n = 1;
    if (i >>> 16 == 0) { n += 16; i <<= 16; }
    if (i >>> 24 == 0) { n +=  8; i <<=  8; }
    if (i >>> 28 == 0) { n +=  4; i <<=  4; }
    if (i >>> 30 == 0) { n +=  2; i <<=  2; }
    // 检查最高位是否为1
    n -= i >>> 31;
    return n;
}
~~~

## 参考

https://www.bilibili.com/video/BV1xV41127u6?spm_id_from=333.337.search-card.all.click

https://blog.csdn.net/zzu_seu/article/details/106698150