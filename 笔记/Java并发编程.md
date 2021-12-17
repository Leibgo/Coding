# Java并发编程实战

## 第一章  简介

利用线程可以充分发挥多处理器系统的强大计算能力。

### 1.1 并发简史

早期的计算机并不包含操作系统，它们从头到尾只执行一个程序，并且这个程序能访问计算机中的所有资源。每次只能运行一个程序，对于计算机资源来说是一种浪费。

操作系统的出现使得计算机每次运行多个程序，并且不同的程序都在单独的进程中运行：**操作系统为每个独立的进程分配各种资源，包括内存，文件句柄以及安全证书等。**

之所以在计算机中加入操作系统来实现多个程序的执行，主要基于以下原因：

- 资源利用率：如果在等待的同时可以运行另一个程序，那么无疑将提高资源的利用率。
- 公平性：不同的用户和程序对于计算机的资源有同样的使用权。一种高效的运行方式是通过粗粒度的时间分片(Time Slicing)是这些用户和程序共享计算机资源
- 便利性：通常来说，在计算多个任务时，编写多个程序，每个程序执行一个任务并在必要时通信。这比只编写一个程序来的简单

<font color=orange>这些促使进程出现的因素，同样也促使线程的出现。线程会共享进程内的资源，例如内存和文件句柄。但每个线程都有各自的程序计数器、栈以及局部变量等。</font>

由于一个进程中的所有线程都将共享进程的内存地址空间，因此这些线程都能访问相同的变量并在一个堆上分配对象。

这就需要实现一种明确的同步机制来协同对共享数据的访问，当一个线程正在使用某个变量时，另一个线程可能同时访问这个变量，这会造成不可预测的后果。

### 1.2 线程的优势

线程是基本的调度单位，多线程程序可以同时在多个处理器上执行。如果设计正确，多线程程序可以提高处理器资源的利用率来提高系统吞吐率。

为模型中每种类型的任务都分配一个专门的线程，那么可以形成串行执行的假象，并将程序的执行逻辑与调度机制的细节，交替执行的操作以及资源等待等问题分离开来。

服务器应用程序在接收来自多个远程客户端的套接字连接请求时，如果为每个连接分配其各自的线程并且使用同步I/O，那么就会降低这类程序的开发难度。

### 1.3 线程的风险

**安全性问题：**

多线程共享相同的地址空间，并且是并发运行的，因此他们可能会访问或修改其他线程正在使用的变量。这是一种极大的便利，因为这种方式比其他进程间通信机制更容易实现数据的分享。<font color=orange>但它同样带来了风险：线程由于无法预料的数据变化而发生错误。是否会返回唯一的值，取决于多个线程的交替执行顺序</font>

**活跃性问题：**

当某个操作无法继续执行下去时，就会发生活跃性问题。

在串行程序中，活跃性问题的形式之一就是无意中造成的无限循环，从而使循环之后的代码无法得到执行。

线程将带来其他一些活跃性问题，包括死锁、饥饿以及活锁。

**性能问题**：

性能问题包括多个方面：例如服务时间过长，响应不灵敏，吞吐率过低，资源消耗过高，或者可伸缩性较低等。

在多线程程序中，会频繁地发生上下文切换操作(Context Switch)，这种操作将带来极大的开销：<font color=orange>保存和恢复执行上下文，</font>CPU时间将更多地花在线程调度而不是线程运行上。

## 第二章 线程安全性

编写线程安全的代码，其核心在于要对状态访问操作进行管理，特别是对<font color=pink>共享的（Shared）和可变的（Mutable）状态</font>的访问。

从非正式的意义上来说，对象的状态是指存储在状态变量（例如实例或者静态域）中的数据。

<font color=orange>"共享"意味着变量可以由多个线程同时访问，而"可变"则意味着变量的值在其生命周期内可以发生变化。</font>

一个对象是否是线程安全的，取决于他是否被多个线程同时访问。这指的是在程序中访问对象的方式，而不是对象要实现的功能。

要使对象是线程安全的，需要采取同步机制来协同对对象可变状态的访问。如果无法实现协同，那么会导致数据被破坏以及其他不该出现的结果。

如果当多个线程访问同一个可变的状态变量时没有使用合适的同步，那么程序就会出现错误。有三种方式修复这个问题：

- 不在线程共享该状态变量
- 将状态变量修改为不可变的变量
- 在访问状态变量时使用同步

### 2.1 什么是线程安全性

<font color=orange>线程安全性：当多个线程访问某个类时，这个类始终表现出正确的行为，那么就称这个类是线程安全的。</font>正确性的含义是，某个类的行为与其规范完全一致。

### 2.2 原子性

竞态条件(Race Condition)：某个计算的正确性取决于多个线程的交替执行时序。换句话说，正确的结果取决于运气。

最常见的竞态条件类型就是“先检查后执行（Check-Then-Act）”

~~~java
@NotThreadSafe
public class LazyInitRace {
    private ExpensiveObject instance = null;
    public ExpensiveObject getInstance(){
        //多个线程同时访问条件判断语句时发生竞态条件
        if(instance == null){
            instance = new ExpensiveObject();
        }
        return instance;
    }
}
~~~

另一种常见的竞态条件类型就是“读取-修改-写入”操作

~~~java
public class UnsafeCountingFactorizer implements Servlet{
    private long count = 0;
    public long getCount(){ return count; }
    public void service(ServletReqeust req, ServletResponse resp){
	   BigInteger i = extractFromRequest(req);
       BigInteger[] factors = factor(i);
       //count++包含三个独立的操作：读取-操作-写入,操作并非原子
       count++;
       encodeIntoResponse(resp, response)
    }
}
~~~

上两种情况都需要包含一组以原子方式执行（或者说不可分割）的操作以确保线程安全性。

### 2.3 加锁

**内置锁**

Java提供了一种内置的锁机制来支持原子性：同步代码块(Synchronized Block)。

同步代码块包含两个部分：一个作为锁对象，一个作为由这个锁保护的代码。

~~~java
synchronized(lock){
    //访问或修改由这个锁保护的共享状态
}
~~~

每个Java对象都可以用作一个实现同步的锁，这些锁被称为内置锁(Intrinsic Lock)或监视器锁(Monitor Lock)。**免去显示地创建锁对象。**

最多只有一个线程能持有这种锁，因此也最多只有一个线程能执行锁保护的代码块，同步代码块会以原子方式执行，多个线程在执行该代码块时不会相互干扰。

~~~java
@ThreadSafe
public class SynchronizedFactorizer extends GenericServlet implements Servlet {
    @GuardedBy("this") private BigInteger lastNumber;
    @GuardedBy("this") private BigInteger[] lastFactors;

    public synchronized void service(ServletRequest req, ServletResponse resp) {
        BigInteger i = extractFromRequest(req);
        if (i.equals(lastNumber))
            encodeIntoResponse(resp, lastFactors);
        else {
            //factor(i)可能会消耗很长时间,因此线程也会占用锁很长时间
            //在多客户端时会带来糟糕的用户体验
            BigInteger[] factors = factor(i);
            lastNumber = i;
            lastFactors = factors;
            encodeIntoResponse(resp, factors);
        }
    }
}
~~~

同一时刻只有一个线程执行`service`方法。虽然这种方式确保线程安全了，但是过于极端，因为factor(i)函数可能会耗费很长时间，剩余的线程只有等待前一个线程执行完毕才能执行，服务的响应非常低。这是性能问题。

**重入**

内置锁是可以重入的，因此如果某个线程试图获得一个已经由它自己持有的锁，那么这个请求就会成功。

<font color=orange>重入意味着锁的操作的粒度是线程，而不是"调用"</font>

重入的一种实现方式是：为每个锁关联一个获取计数值和一个所有值线程。当计数值为0时，这个锁就认为是没有被任何线程持有。当线程请求一个未被持有的锁时，JVM 将记下锁的持有者，并且将获取计数值置为1。如果同一个线程在此获取这个锁，计数值将递增，当线程退出同步代码块时，计数器将会递减。当计数值为0时，这个锁将被释放。

~~~java
public class Widget{
    public synchronized void doSomething(){
        ...
    }
}
public class LoggingWidget extends Widget{
    public synchronized void doSomething(){
        System.out.println("do Something...");
        //再次获取Widget上的锁
        super.doSomething();
    }
}
~~~

### 2.4 用锁来保护状态

<font color=orange>一个常见的加锁约定是：将所有的可变状态都封装在对象内部，并通过对象的内置锁对所有访问可变状态的代码路径进行同步，使得在该对象上不会发生并发访问。</font>例如 Vector类或者其他的同步集合类。

但是`synchronize`虽然可以确保单个操作的原子性，但如果把多个操作合并为一个复合操作，还是需要额外的加锁机制。

~~~java
//需要额外的加锁机制
if(!vector.contains(element)){
    vector.add(element);
}
~~~

此外将每个方法都作为一个同步方法还可能导致活跃性或者性能问题。

### 2.5 活跃性与性能

在加锁一小节中曾介绍过极端的加锁会带来不好的性能问题。当多个请求同时到达时，只有一个请求可以执行，必须等待前一个请求执行完成。

将这种Web应用程序称为<font color=pink>不良并发(Poor Concurrency)应用程序</font>：可同时调用请求的数量，不仅受到处理资源的限制，还受到应用程序本身结构的限制。

**可以通过缩小同步代码块的作用范围，做到既确保应用程序的并发性，同时又能维护线程安全性。**

<font color=orange>应该尽量将不影响共享状态且执行时间较长的操作从同步代码块中分离出去。如factor(i)</font>

~~~java
@ThreadSafe
public class SynchronizedFactorizer extends GenericServlet implements Servlet {
    @GuardedBy("this") private BigInteger lastNumber;
    @GuardedBy("this") private BigInteger[] lastFactors;

    public void service(ServletRequest req, ServletResponse resp) {
        //局部变量是线程安全的
        BigInteger i = extractFromRequest(req);
        BigInteger[] factors = null;
        //两个同步块
        synchronized(this){
            if (i.equals(lastNumber)){
                facotrs = lastFactors.clone();
            }
        }
        if(factors == null){
            //在执行较长时间的运算之前释放了锁,因此其他线程可以访问上一个代码块
            factors = factor(i);
            synchronized(this){
                lastNumber = i;
            	lastFactors = factors.clone();
            }
        }
        encodeIntoResponse(resp, factors);
    }
}
~~~

## 第三章 对象的共享

第二章介绍了如何通过同步来避免多个线程在同一时刻访问相同的数据。

本章将介绍<font color=orange>如何共享和发布对象，从而使他们能够安全地由多个线程同时访问。</font>

同步还有另一个重要的方面：<font color=orange>内存可见性（Memory Visibility）</font>。我们不仅希望防止某个线程正在使用对象状态而另一个线程在同时修改状态，而且<font color=orange>希望确保当一个线程修改完了对象的状态后，其他线程能够看到发生的状态变化。</font>

### 3.1 可见性

Java内存模型规定：所有变量都存储在主内存中，每个线程都有自己的工作内存。工作内存保存了线程使用的变量，<font color=orange>线程对变量的读取必须在工作内存而不是直接读取主存中的变量。</font>不同的线程无法访问对方工作内存。

<img src="https://s2.loli.net/2021/12/17/jwHcSfmhokrxbTA.png" alt="image-20211217140203332" style="zoom:80%;float:left" />

线程A和线程B通信要经过两个步骤：

- 线程A把本地内存中更新过的共享变量刷新到主内存中
- 线程B到主内存中去读线程A之前已更新过的共享变量

**加锁与可见性**

<font color=orange>使用`synchronized`代码块时，会首先清空本地内存中的块中共享变量的值，在执行使用变量前会重新从主内存中读取，在释放锁前把变量的值刷新到主内存中。</font>

加锁的含义不仅仅局限于互斥行为，还包括内存可见性。

代码中没有使用足够的同步机制，因此无法确保主线程写入的ready值和number值对于读线程来说是可见的

~~~java
public class NoVisibility {
    private static boolean ready;
    private static int number;

    private static class ReaderThread extends Thread {
        public void run() {
            while (!ready)
                Thread.yield();
            System.out.println(number);
        }
    }

    public static void main(String[] args) {
        //读线程可能无法读取到number和ready的值
        new ReaderThread().start();
        number = 42;
        ready = true;
    }
}
~~~

我们只是说读线程可能无法读取到number和ready的值，但由于存在重排序现象，读线程会有一定的几率读取到更新后的两个值。

~~~java
//重排序后
number = 42;
ready = true;
new ReaderThread().start();
~~~

在没有同步的情况下，编译器、处理器以及运行时都可能会对操作的执行顺序进行一下意想不到的调整。在缺乏足够同步的多线程程序中，要想对内存操作的执行顺序进行判断，几乎无法得出正确的结论。这种现象被称为"重排序"。

**volatile变量**

Java语言提供了一种稍弱的同步机制，即`volatile`变量，用来确保变量的更新操作通知到其他线程中。

`volatile`关键字<font color=orange>保证新值立即更新到主内存中，以及每个线程在使用volatile变量时都立即从主内存刷新。</font>这就意味着volatile变量不会被缓存到工作内存中。

除了可见性外，当把变量声明为`volatile`类型后，编译器与运行时都会注意到这个变量是共享的，因此不会将变量上的操作与其他内存操作一起重排序。保证了有序性。

<font color=pink>加锁机制(synchronized)保证了原子性和可见性，volatile保证了可见性和有序性。</font>

### 3.2 发布与逸出

“发布(Publish)”一个对象的意思是指，使对象能够在当前作用域之外的代码中使用。

~~~java
class Secrets {
    //方式一：发布对象：对象的引用保存到了公有的静态变量中
    public static Set<Secret> knownSecrets;

    public void initialize() {
        knownSecrets = new HashSet<Secret>();
    }
}
~~~

"逸出(Escape)"指某个不应该发布的对象被发布了。

~~~java
class UnsafeStates {
    private String[] states = new String[]{
        "AK", "AL" /*...*/
    };
	//方式二：发布对象：从非私有方法中返回一个引用
    public String[] getStates() {
        return states;
    }
}
~~~

数组states已经逸出了它所在的 private 作用域，因为本应私有的变量被发布了。同时也出现了问题，任何调用者都可以修改这个数组的内容。

~~~java
public class ThisEscape {
    //方式三：发布对象：发布一个内部的类实例。
    public ThisEscape(EventSource source) {
        source.registerListener(new EventListener() {
            public void onEvent(Event e) {
                doSomething(e);
            }
        });
    }

    void doSomething(Event e) {
    }


    interface EventSource {
        void registerListener(EventListener e);
    }

    interface EventListener {
        void onEvent(Event e);
    }

    interface Event {
    }
}
~~~

当 ThisEscape 发布 EventListener 时，也隐含地发布了 ThisEscape 实例本身，因为在这个内部类的实例中包含了对 ThisEscape 实例的隐含引用。

这个例子给出了逸出的特殊示例，即 this 引用在构造函数中逸出。<font color=orange>当从对象的构造函数发布对象时，只是发布了一个尚未构造完成的对象。</font>

如果 this 引用在构造过程中逸出，那么这种对象就被认为是不正确构造，只有当构造函数返回时，this 引用才应该从线程中逸出。

如果想在构造函数中注册一个事件监听器或者启动线程，那么可以使用私有的构造方法和公共的静态工厂方法来防止 this 引用的逸出

~~~java
public class SafeListener{
    private final EventListener listener;
    
    private SafeListener() {
        // this引用逸出，但被保存在了listener中，不会被其他线程在构造函数完成之前使用它
        listener = new EventListener() {
            public void onEvent(Event e) {
                doSomething(e);
            }
        };
    }

    public static SafeListener newInstance(EventSource source) {
        SafeListener safe = new SafeListener();
        source.registerListener(safe.listener);
        return safe;
    }
}
~~~

### 3.3 线程封闭

当访问共享的可变数据时，通常需要用到同步。一种避免使用同步的方式就是不共享数据。<font color=orange>如果仅在单线程内访问数据，就不需要同步。</font>

这种技术称为线程封闭(Thread Confinement)，它是实现线程安全性的最简单方式之一。

当某个对象封闭在一个线程中时，这种用法将自动实现线程安全性，即使被封闭的对象本身不是线程安全的。

**1. Ad-hoc 线程封闭**

Ad-hoc 线程封闭是指，维护线程封闭性的职责完全由程序实现来承担。

在volatile变量上存在一种特殊的线程封闭，只要能确保只有一个线程能够对共享的volatile变量执行写入操作，那么就可以安全的在这些共享的volatile变量上执行修改操作。

这种情况相当于将修改操作封闭在当个线程以防止发生竞态条件，并且volatile的可见性保证了其他线程可以看到最新的值。

**2. 栈封闭**

栈封闭是线程封闭的一个特性，在栈封闭中，<font color=orange>只能通过局部变量才能访问对象。</font>局部变量的固有属性之一就是封闭在执行线程中。

对于基本类型的局部变量，无论如何都不会破坏栈的封闭性。由于任何方法都无法获得对基本类型的引用，因此Java语言的这种语义确保了基本类型的局部变量始终封闭在线程内。

~~~java
public int loadTheArk(Collection<Animal> candidates) {
    SortedSet<Animal> animals;
    //基本类型的变量无法获得引用
    int numPairs = 0;
    Animal candidate = null;

    // 不要让引用对象animal逸出
    animals = new TreeSet<Animal>(new SpeciesGenderComparator());
    animals.addAll(candidates);
    for (Animal a : animals) {
        if (candidate == null || !candidate.isPotentialMate(a))
            candidate = a;
        else {
            ark.load(new AnimalPair(candidate, a));
            ++numPairs;
            candidate = null;
        }
    }
    return numPairs;
}
~~~

当局部变量是一个对象引用时，需要确保引用对象不会逸出。

如果发布了对集合 animal (或者该对象中的任何数据)的引用，那么封闭性就将破坏，并导致animal对象的逸出。

**3. ThreadLocal类**

维护线程封闭性的一种更规范方法是使用 ThreadLocal。

每个 Thread 内有自己的实例副本，且该副本只能由当前 Thread 使用，这也是 ThreadLocal 命名的由来。

ThreadLocal 提供了线程本地的实例。它与普通变量的区别在于，每个使用该变量的线程都会初始化一个完全独立的实例副本。

ThreadLocal 变量通常被<font color=pink>private static</font>修饰。当一个线程结束时，它所使用的所有 ThreadLocal 相对的实例副本都可被垃圾回收。

<font color=orange>ThreadLocal 适用于每个线程需要自己独立的实例且该实例需要在多个方法中被使用，即变量在线程间隔离而在方法或类间共享的场景。</font>

<img src="https://s2.loli.net/2021/12/17/D3gYhSekMoVsGB6.png" alt="img" style="zoom:80%;float:left" />

<font color="orange">一句话理解ThreadLocal，向ThreadLocal里面存东西就是向它里面的Map存东西的，然后ThreadLocal把这个Map挂到当前的线程底下，这样Map就只属于这个线程了。</font>

~~~java
public class ThreadLocaDemo {
 
    private static ThreadLocal<String> localVar = new ThreadLocal<String>();
 
    static void print(String str) {
        //打印当前线程中本地内存中本地变量的值
        System.out.println(str + " :" + localVar.get());
        //清除本地内存中的本地变量
        localVar.remove();
    }
    public static void main(String[] args) throws InterruptedException {
 
        new Thread(new Runnable() {
            public void run() {
                ThreadLocaDemo.localVar.set("local_A");
                print("A");
                //打印本地变量
                System.out.println("after remove : " + localVar.get());
               
            }
        },"A").start();
 
        Thread.sleep(1000);
 
        new Thread(new Runnable() {
            public void run() {
                ThreadLocaDemo.localVar.set("local_B");
                print("B");
                System.out.println("after remove : " + localVar.get());
              
            }
        },"B").start();
    }
}
 
A :local_A
after remove : null
B :local_B
after remove : null
~~~

**ThreadLocal :: set ( )**

~~~java
public void set(T value) {
    //获取当前线程
    Thread t = Thread.currentThread();
    //获取当前线程的ThreadLocalMap
    ThreadLocalMap map = getMap(t);
    if (map != null)
        //以threadlocal为key将value添加到map
        map.set(this, value);
    else
        createMap(t, value);
}
~~~

**ThreadLocal :: get( )**

~~~java
public T get() {
    //获取当前线程
    Thread t = Thread.currentThread();
    //获取当前线程的ThreadLocalMap
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        //获取以threadlocal为key的entry
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    //若entry为空，说明threadlocalmap中还未存入以threadlocal为key的键值对，添加
    return setInitialValue();
}
~~~

**ThreadLocal : : remove( )** 

~~~java
public void remove() {
    //获取当前线程的ThreadLocalMap
    ThreadLocalMap m = getMap(Thread.currentThread());
    //清除threadlocal为key保存的内容
    if (m != null)
        m.remove(this);
}
~~~

**ThreadLocalMap : : set( )**

~~~java
private void set(ThreadLocal<?> key, Object value) {
    //将table数组引用传给tab，table中的每一个entry代表一个threadlocal生成的key以及存入的value
    Entry[] tab = table;
    int len = tab.length;
    //计算当前threadloca的哈希值,作为tab的索引
    int i = key.threadLocalHashCode & (len-1);
	//如果tab[i]元素存在，说明之前threadlocal已经存过值
    for (Entry e = tab[i];e != null;e = tab[i = nextIndex(i, len)]) {
        //获取对应的threadlocal
        ThreadLocal<?> k = e.get();
		//返回存入的value
        if (k == key) {
            e.value = value;
            return;
        }
		//threadlocal为null
        if (k == null) {
            replaceStaleEntry(key, value, i);
            return;
        }
    }
	//tab[i]不存在，说明threadlocal未存过值
    tab[i] = new Entry(key, value);
    int sz = ++size;
    if (!cleanSomeSlots(i, sz) && sz >= threshold)
        rehash();
}
~~~

<img src="https://s2.loli.net/2021/12/17/B9HxFtLofm2eyQs.png" alt="image-20211217220111864" style="float:left" />



