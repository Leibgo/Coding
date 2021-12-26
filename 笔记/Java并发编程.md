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

在良好的规范中通常会定义各种不变性条件(Invariant)来约束对象的状态，以及各种后验条件(Postcondition)来描述对象操作的结果。

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

<font color=orange>在许多情况中，我们需要确保对象及其内部状态不被发布。而在某些情况下，我们又需要发布某个对象，这时就需要用到同步来确保线程安全，使得程序维持不变性条件。</font>

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

### 3.4 不变性

满足同步需求的另一种方法是使用不可变对象(Immutable Object)。

<font color=orange>如果某个对象在被创建后其状态不能被修改，那么这个对象就称为不可变对象。线程安全性是不可变对象的固有属性之一。</font>

当满足以下条件时，对象才是可变的：

- 对象创建以后其状态就不能修改，只提供读方法不提供写的方法
- 对象的所有域都是final类型的
- 对象是正确创建的

~~~java
@Immutable
public final class ThreeStooges {
    //private属性
    private final Set<String> stooges = new HashSet<String>();
	//初始化时创建相同的三个元素
    public ThreeStooges() {
        stooges.add("Moe");
        stooges.add("Larry");
        stooges.add("Curly");
    }
	//不提供get方法使stooges逸出，set对象构造完成以后无法对其进行修改
    public boolean isStooge(String name) {
        return stooges.contains(name);
    }
}
~~~

**Final 域**

被final修饰的变量，基本类型变量无法重新赋值，引用对象无法重新引用但可以修改其状态。

在Java内存模型中，final 域还有特殊的语义。final 域能确保初始化过程的安全性，从而可以不受限制地访问不可变对象，并且共享时无需同步。

- 如果某个成员变量用final修饰，<font color=orange>JVM规范做出如下明确的保证：一旦该变量的引用对象对其他线程可见，则其final成员也必须已经完成初始化了，可以被其他线程看到</font>

- final关键字禁止 CPU 指令集重新排序,来保证对象的安全发布，防止对象引用被其他线程在对象被完全构造完成前拿到并使用。

- final修饰的成员变量还有另一个特性，如果是基本类型，则值不能改变。如果是引用类型，则地址不能改变。

final关键字保证了多线程下变量的数据安全性

~~~java
@Immutable
public class OneValueCache {
    //final修饰两个变量
    private final BigInteger lastNumber;
    private final BigInteger[] lastFactors;
	//构造方法
    public OneValueCache(BigInteger i, BigInteger[] factors) {
        lastNumber = i;
        lastFactors = Arrays.copyOf(factors, factors.length);
    }
	//只提供读方法不提供修改的方法
    public BigInteger[] getFactors(BigInteger i) {
        if (lastNumber == null || !lastNumber.equals(i))
            return null;
        else
            return Arrays.copyOf(lastFactors, lastFactors.length);
    }
}
~~~

<font color=orange>对于在访问和更新多个变量时出现的竞态条件问题，可以通过将这些变量全部保存到一个不可变对象中来消除。</font>

不用担心其他线程会修改对象的状态，如果更新这些变量，就创建一个新的容器对象。

~~~java
@ThreadSafe
public class VolatileCachedFactorizer extends GenericServlet implements Servlet {
    //volatile修饰，确保其他线程可以见到更新后的数据
    private volatile OneValueCache cache = new OneValueCache(null, null);
	
    public void service(ServletRequest req, ServletResponse resp) {
        BigInteger i = extractFromRequest(req);
        BigInteger[] factors = cache.getFactors(i);
        if (factors == null) {
            factors = factor(i);
            //创建新的容器cache
            cache = new OneValueCache(i, factors);
        }
        encodeIntoResponse(resp, factors);
    }
}
~~~

### 3.5 安全发布

在某些情况下我们希望在多个线程间共享对象，此时必须确保安全地进行共享。

~~~java
public class StuffIntoPublic {
    //不安全的发布
    public Holder holder;
	//不正确的发布将导致其他线程看到尚未完成创建完成的对象
    public void initialize() {
        holder = new Holder(42);
    }
}
~~~

在 `initialize` 执行的线程，`holder` 得到一个新对象的引用，一定在 `Holder` 的构造结束之后。
<font color=orange>但是，由于这里没有额外的同步机制，在**另一个线程**里，看到 holder 里拿到一个非空引用的时候，不一定能看到 `Holder` 构造里所有语句的结果（对 n 的赋值）。</font>

**不正确的发布**

没有使用同步来确保 Holder 对象对其他线程可见，因此称"Holder" 为“未被正确发布”。

未被正确发布的对象存在两个问题

1. 其他线程可能看到的holder是一个失效值，因此将看到一个空引用或者是null
2. 看到的引用是新的，但引用的状态却是失效的
3. 线程在第一次读取某个域时是失效的，第二次读取这个域时却是一个更新值，这也是抛出异常的原因

~~~java
public class Holder {
    private int n;

    public Holder(int n) {
        this.n = n;
    }
	//调用这个方法时将抛出异常
    public void assertSanity() {
        if (n != n)
            throw new AssertionError("This statement is false.");
    }
}
~~~

**不可变对象与初始化安全性**

如果Holder对象是不可变的，即将n声明为final类型，那么即使Holder类没有被正确的发布，也可以安全地访问该对象。

~~~java
public class Holder {
    private final int n;

    public Holder(int n) {
        this.n = n;
    }
	
    public void assertSanity() {
        if (n != n)
            throw new AssertionError("This statement is false.");
    }
}
~~~

**安全发布的常用模式**

可变对象必须通过安全的方式来发布，这就意味着发布和使用对象时都必须使用同步。

- 在静态初始化函数中初始化一个对象引用（由JVM在类的初始化阶段执行） `public static Holder holder = new Holder(42);`
- 将对象的引用保存到 volatile 类型的域或者 AtomicReference 对象中 `public volatile Holder holder;`
- 将对象的引用保存到某个正确构造对象的final域中 `public final Holder holder;`
- 将对象的引用保存到一个由锁保护的域中 

**事实不可变对象**

如果对象在发布后不会被修改，那么在没有额外同步的情况下访问这些对象的线程来说，安全发布是足够的

<font color=orange>所有的安全发布机制都能确保，当对象的引用对所有访问该状态的线程可见时，对象发布时的状态对所有线程也是可见的，并且如果对象的状态不再改变，那么就足以确保任何访问都是安全的</font>

事实不可变对象：从技术上来看是可变的，但其状态在发布后不会再改变。

**可变对象**

对象的发布需求取决于它的可变性

- 不可变对象可以通过任意机制来发布
- 事实不可变对象必须通过安全发布方式来发布
- 可变对象必须通过安全方式来发布，并且必须是线程安全的或者由某个锁保护起来确保后续访问的可见性

## 第四章 对象的组合

本章将介绍一些组合模式，这些模式能够使一个类更容易成为线程安全的，并且在维护这些类时，不会无意中破坏类的安全性保证。

### 4.1 设计线程安全的类

通过使用封装技术，可以使得在不对整个程序进行分析的情况下就可以判断一个类是否是线程安全的。

- 找出构成对象状态的所有变量
- 找出约束状态变量的不变性条件
- 建立对象状态的并发访问管理策略

要确保类的线程安全性，就需要确保它的不变性条件不会在并发访问的情况下被破坏，这需要对它的状态进行推断。

在许多类中都定义了一些不变性条件，来判断状态是否是有效的还是无效的。同样在操作中会包含一些后验条件来判断状态的迁移是否是有效的，当下一个状态依赖于前一个状态时，这个操作必须是复合操作。

<font color=orange>由于不变性条件以及后验条件在状态及状态转换上施加了各种约束，因此就需要额外的同步与封装。</font>

在某些对象的方法中还包含一些基于状态的先验条件(Precondition)。例如不能从空队列中移除一个元素。

在单线程中，如果某个操作无法满足先验条件，那么只能失败。但在并发程序中，先验条件可能会由于其他线程执行的操作而变成真。在并发程序中，要一直等到先验条件为真，然后再执行该操作。

### 4.2 实例封闭

封装简化了线程安全类的实现过程，他提供了一种<font color=pink>实例封闭机制（Instance Confinement），简称为"封闭"。</font>

将数据封装在对象内部，可以将数据的访问限制在对象的方法上，从而更容易确保线程在访问数据时总能持有正确的锁。

被封闭的对象一定不能超出它们既定的作用域，可以封闭在类的实例、封闭在某个作用域(局部变量)或者封闭在线程内。

<font color=orange>通过将封闭机制与合适的加锁策略结合起来，可以确保以线程安全的方式来使用非线程安全的对象。</font>

~~~java
@ThreadSafe
public class PersonSet {
    @GuardedBy("this") 
    private final Set<Person> mySet = new HashSet<Person>();

    public synchronized void addPerson(Person p) {
        mySet.add(p);
    }

    public synchronized boolean containsPerson(Person p) {
        return mySet.contains(p);
    }
}
~~~

如上代码所示，HashSet 不是线程安全的，但由于 mySet 是私有的且不会逸出，因此 HashSet 被封闭在 PersonSet 中。唯一能访问 mySet 的代码路径是`addPerson`和`containsPerson`，执行时都需要获得他们的锁。

<font color=orange>PersonSet的状态完全由它的内置锁保护，PersonSet 是一个线程安全的类。</font>

这里并未对 Person 的线程安全性做任何假设，如果 Person 类是可变的，那么在访问从 PersonSet 中获得 Person 对象时，需要额外的同步。

<font color=orange>实例封闭是构建线程安全类的一个最简单方法。</font>

在Java平台的类库中还有很多线程封闭的示例，<font color=orange>其中有些类的唯一用途就是将非线程安全的类转换为线程安全的类。</font>

~~~java
static class SynchronizedList<E> extends SynchronizedCollection<E> implements List<E> {
	//将非线程安全的容器类如ArrayList包装进同步的包装器对象中
    final List<E> list;
	//构造函数
    SynchronizedList(List<E> list) {
        super(list);
        this.list = list;
    }
    //对list的访问
    public E set(int index, E element) {
        //使用mutex保证同步
        synchronized (mutex) {
            return list.set(index, element);
        }
   	}
    public void add(int index, E element) {
        synchronized (mutex) {
            list.add(index, element);
        }
    }
    ...
}
    
~~~

当然如果将一个本该封闭的对象发布出去，那么也能破坏封闭性

**Java监视器模式**

从线程封闭原则及其逻辑推论可以得出<span name="Java监视器">Java监视器模式。</span>

遵循Java监视器模式的对象会把对象的所有可变状态都封装起来，并由对象自己的内置锁来保护，例如`Vector`和`HashTable`。

~~~java
public class Hashtable<K,V> extends Dictionary<K,V> implements Map<K,V>, Cloneable, java.io.Serializable {
	//私有属性
    private transient int count;
    private int threshold;
    ...
    //使用内置锁保护
    public synchronized int size() {
        return count;
    }
    public synchronized boolean isEmpty() {
        return count == 0;
    }
    //Java监视器的优势在于它很简单
}
~~~

对于任何一种锁，只要自始至终都使用该锁对象，都可以用来保护对象的状态。Java监视器仅仅是一种代码规范而已，也可以使用私有锁来保护状态。

~~~java
public class PrivateLock {
    //私有的锁对象可以将锁也封装起来，是客户代码无法得到
    private final Object myLock = new Object();
    @GuardedBy("myLock") Widget widget;

    void someMethod() {
        synchronized (myLock) {
            // 访问
        }
    }
}
~~~

**基于Java监视器模式的车辆追踪**

~~~java
@ThreadSafe
public class MonitorVehicleTracker {
    @GuardedBy("this")
    //Map对象与MutablPoint对象都没有对外发布
    private final Map<String, MutablePoint> locations;

    public MonitorVehicleTracker(Map<String, MutablePoint> locations) {
        this.locations = deepCopy(locations);
    }

    //获得所有车的定位
    public synchronized Map<String, MutablePoint> getLocations() {
        return deepCopy(locations);
    }

    //获得某一辆车的定位
    public synchronized MutablePoint getLocation(String id) {
        MutablePoint loc = locations.get(id);
        //返回新的MutablePoint对象,防止逸出
        return loc == null ? null : new MutablePoint(loc);
    }
    //设置某一辆车的定位
    public synchronized void setLocation(String id, int x, int y) {
        MutablePoint loc = locations.get(id);
        if (loc == null)
            throw new IllegalArgumentException("No such ID: " + id);
        loc.x = x;
        loc.y = y;
    }
    //深度拷贝
    private static Map<String, MutablePoint> deepCopy(Map<String, MutablePoint> m) {
        //返回新的Map对象
        Map<String, MutablePoint> result = new HashMap<String, MutablePoint>();

        for (String id : m.keySet()){
            result.put(id, new MutablePoint(m.get(id)));
        }
        return Collections.unmodifiableMap(result);
    }
}
//线程不安全的位置类
@NotThreadSafe
public class MutablePoint {
    public int x, y;

    public MutablePoint() {
        x = 0;
        y = 0;
    }

    public MutablePoint(MutablePoint p) {
        this.x = p.x;
        this.y = p.y;
    }
}

~~~

<font color=orange>虽然Point类是线程不安全的，追踪器类是线程安全的。</font>Map对象与 MutablPoint 对象都没有对外发布，需要返回车辆或者位置时，通过 `deepCopy` 方法复制正确的值，从而生成一个新的Map对象。

### 4.3 线程安全性的委托

Java监视器模式特别适用于将多个非线程安全类组合为一个类时的情况。

但是，如果类中的各个组件都已经是线程安全的，该怎么办？需要还需要添加一个额外的线程安全层？----------视情况而定

**基于委托的车辆跟踪器**

~~~java
@ThreadSafe
public class DelegatingVehicleTracker {
    //变量locations是线程安全类
    //由于point是不可变对象，因此发布指向不可变对象的引用是不会破坏封装性的
    //Map中所有的键和值都是不可变的
    private final ConcurrentMap<String, Point> locations;
    private final Map<String, Point> unmodifiableMap;
	
    public DelegatingVehicleTracker(Map<String, Point> points) {
        locations = new ConcurrentHashMap<String, Point>(points);
        unmodifiableMap = Collections.unmodifiableMap(locations);
    }
	
    //方法没有使用任何的同步,对状态的访问都由ConcurrentHashMap来管理
    public Map<String, Point> getLocations() {
        return unmodifiableMap;
    }
	
    public Point getLocation(String id) {
        return locations.get(id);
    }

    public void setLocation(String id, int x, int y) {
        if (locations.replace(id, new Point(x, y)) == null)
            throw new IllegalArgumentException("invalid vehicle name: " + id);
    }
}
//线程安全类Point
@Immutable
public class Point {
    //不可变属性
    public final int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

~~~

注意：使用委托的车辆跟踪器中返回的是一个不可修改但实时的车辆位置试图。

这意味着，如果线程A调用`getLocation`，而线程B随后修改了点的位置，那么返回给线程A的视图中将反应这些变化。<font color=orange>因为指向的是同一块内存地址</font>

<img src="https://s2.loli.net/2021/12/20/yPf1KDdZSqjH9B6.png" alt="image-20211220210316928" style="float:left" />

~~~java
//如果需要不发生变化的视图，则可以返回对location对象的浅拷贝
public Map<String, Point> getLocationsAsStatic() {
    return Collections.unmodifiableMap(new HashMap<String, Point>(locations));
}
~~~

![image-20211220205858709](https://s2.loli.net/2021/12/20/vr1SLkmVy4c3Z2e.png)

**委托给多个状态变量**

刚刚的示例是将线程安全性委托给了单个线程安全的状态变量。

<font color=orange>我们也可以将线程安全性委托给多个状态变量，只要这些变量是彼此独立的，即组合而成的类并不会在其包含的多个状态变量上增加任何不变性条件</font>

~~~java
public class VisualComponent {
    //CopyOnWriteArrayList是一个线程安全的链表,两个链表之间也不存在耦合关系
    //因此VisualComponent将它的线程安全性委托给了它的两个状态
    private final List<KeyListener> keyListeners = new CopyOnWriteArrayList<KeyListener>();
    private final List<MouseListener> mouseListeners = new CopyOnWriteArrayList<MouseListener>();
	
    public void addKeyListener(KeyListener listener) {
        keyListeners.add(listener);
    }

    public void addMouseListener(MouseListener listener) {
        mouseListeners.add(listener);
    }

    public void removeKeyListener(KeyListener listener) {
        keyListeners.remove(listener);
    }

    public void removeMouseListener(MouseListener listener) {
        mouseListeners.remove(listener);
    }
}
~~~

**委托失效**

~~~java
public class NumberRange {
    // 两个变量之间的不变性条件：lower <= upper
    private final AtomicInteger lower = new AtomicInteger(0);
    private final AtomicInteger upper = new AtomicInteger(0);

    public void setLower(int i) {
        // 不安全的"先检查后执行"，这个类必须提供自己的加锁机制以保证复合操作的原子性
        if (i > upper.get())
            throw new IllegalArgumentException("can't set lower to " + i + " > upper");
        lower.set(i);
    }

    public void setUpper(int i) {
        // 不安全的"先检查后执行"
        if (i < lower.get())
            throw new IllegalArgumentException("can't set upper to " + i + " < lower");
        upper.set(i);
    }

    public boolean isInRange(int i) {
        return (i >= lower.get() && i <= upper.get());
    }
}
~~~

虽然`AtomicInteger`是线程安全的，但组合而成的类却不是，因为没有维持下界和下界进行约束的不变性条件。

<font color=orange>状态变量`lower`和`upper`不是彼此独立的，因此`NumberRange`不能将线程安全性委托给他的线程安全状态变量。</font>

**发布底层的状态变量**

当将线程安全性委托给某个对象的底层状态变量时，<font color=orange>在什么条件下可以发布这些变量从而使其他类可以修改它们?</font> -------- 取决于施加了哪些不变性条件

前面的例子中，`VisualComponent` 可以发布它的状态变量，因为它的两个状态变量之间不存在任何的不变性条件。

<font color=pink>如果一个状态变量是线程安全的，并且没有任何不变性条件来约束它的值，在变量的操作上也不存在任何不允许的状态转换，那么就可以安全的发布这个变量</font>

~~~java
@ThreadSafe
public class PublishingVehicleTracker {
    private final Map<String, SafePoint> locations;
    private final Map<String, SafePoint> unmodifiableMap;
	//将线程安全性委托给了底层的ConcurrentHashMap
    public PublishingVehicleTracker(Map<String, SafePoint> locations) {
        this.locations = new ConcurrentHashMap<String, SafePoint>(locations);
        this.unmodifiableMap = Collections.unmodifiableMap(this.locations);
    }

    public Map<String, SafePoint> getLocations() {
        return unmodifiableMap;
    }
	//发布了线程安全且可变的point类，调用者可以修改point类的位置
    public SafePoint getLocation(String id) {
        return locations.get(id);
    }

    public void setLocation(String id, int x, int y) {
        if (!locations.containsKey(id))
            throw new IllegalArgumentException("invalid vehicle name: " + id);
        locations.get(id).set(x, y);
    }
}
@ThreadSafe
public class SafePoint {
    @GuardedBy("this")
    private int x, y;

    private SafePoint(int[] a) {
        this(a[0], a[1]);
    }

    public SafePoint(SafePoint p) {
        this(p.get());
    }

    public SafePoint(int x, int y) {
        this.set(x, y);
    }
	// 同步
    public synchronized int[] get() {
        return new int[]{x, y};
    }

    public synchronized void set(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
~~~

### 4.4 在现有的线程安全类中添加功能

有时候，某个线程安全的类能支持我们需要的所有操作，但更多时候，现有的类只能支持大部分的操作，<font color=orange>此时就需要在不破坏线程安全性的情况下添加一个新的操作。</font>

添加功能的四种方式：

- 修改原始的类
- 扩展这个类
- 客户端加锁机制
- 组合

第一种方法通常无法做到，因为你无法访问或者修改类的源代码。

第二种方法会比较脆弱。因为同步策略实现被分布到了多个单独维护的源代码文件中，如果底层的类改变了同步策略：使用不同的锁来保护它的状态，那么子类就无法使用正确的锁来控制对基类状态的并发访问。

~~~java
@ThreadSafe
// 第二种方式:通过继承扩展这个类
public class BetterVector <E> extends Vector<E> {
    public synchronized boolean putIfAbsent(E x) {
        boolean absent = !contains(x);
        if (absent)
            add(x);
        return absent;
    }
}
~~~

**客户端加锁机制**

第三种策略是扩展类的功能，但并不是扩展类的本身，而是将扩展代码放入一个"辅助类"中。

~~~java
@NotThreadSafe
class BadListHelper <E> {
    //状态变量list是线程安全的，但badListHelper是线程不安全的
    public List<E> list = Collections.synchronizedList(new ArrayList<E>());
	//使用的是listHepler的内置锁，但list使用的是它的内置锁来同步
    public synchronized boolean putIfAbsent(E x) {
        boolean absent = !list.contains(x);
        if (absent)
            list.add(x);
        return absent;
    }
}
~~~

上述代码是线程不安全的，因为使用了不同的锁来进行同步。

<font color=orange><span name="客户端加锁">客户端加锁</span>：对于使用某个对象X的代码，使用X保护其自身状态的锁来保护这段客户代码。</font>

查看Vector或封装性容器的源码，它们是使用内置锁来支持客户端加锁。

~~~java
@ThreadSafe
class GoodListHelper <E> {
    public List<E> list = Collections.synchronizedList(new ArrayList<E>());
	
    public boolean putIfAbsent(E x) {
        // 使用的是list的内置锁
        synchronized (list) {
            boolean absent = !list.contains(x);
            if (absent)
                list.add(x);
            return absent;
        }
    }
}
~~~

然而客户端加锁更加脆弱，因为它将类C的加锁代码放到与C完全无关的其它类中。

**组合**

~~~java
@ThreadSafe
public class ImprovedList<T> implements List<T> {
    private final List<T> list;
	
    //将list对象委托给底层的List实例来实现操作
    public ImprovedList(List<T> list) { this.list = list; }
	//通过自身的内部锁增加了一层额外的锁
    public synchronized boolean putIfAbsent(T x) {
        boolean contains = list.contains(x);
        if (!contains)
            list.add(x);
        return !contains;
    }
    
    public int size() {
        return list.size();
    }

    public synchronized boolean add(T e) {
        return list.add(e);
    }
	...
}
~~~

`ImprovedList`本身并不关心底层的`List`是否是线程安全的，即使`List`不是线程安全的或者修改了它的加锁实现，`ImprovedList`也会提供一致的加锁机制实现它的线程安全性。

<font color=orange>额外的同步会导致轻微的性能损失，但与模拟另一个对象的加锁策略相比，ImprovedList 更为健壮。</font>

## 第五章 基础构建模块

本章将介绍在平台库中提供的一些基础的并发构建模块，包括线程安全的容器类和同步工具类。

### 5.1 同步容器类

同步容器类包括`Vector`、`Hashtable`以及同步封装器类`Collections.synchronizedXxx`

这些类实现线程安全的方式是：<a href="#Java监视器"><font color=orange>将他们的状态封装起来，并对每个公有方法都进行同步，使得每次只有一个线程能访问容器的状态。</font></a>

**同步容器类的问题**

同步容器类虽然是线程安全的，但可能需要额外的客户端加锁来保护<font color=orange>复合操作</font>。

常见的复合操作包括：

- 迭代
- 跳转
- 条件运算(例如"先检查再运算")

~~~java
//Vector中定义的两个方法
public class UnsafeVectorHelpers {
    //获取最后一个元素
    public static Object getLast(Vector list) {
        int lastIndex = list.size() - 1;
        return list.get(lastIndex);
    }
	//删除最后一个元素
    public static void deleteLast(Vector list) {
        int lastIndex = list.size() - 1;
        list.remove(lastIndex);
    }
}
~~~

无论多少线程同时调用这两个方法，都不会破坏 Vector .

但对于方法的调用者来说，在并发环境下调用这两个方法会出现不希望出现的结果。

<img src="https://s2.loli.net/2021/12/21/Q8SUAXWPJOkIRcp.png" alt="image-20211221214232025" style="float:left" />

<font color=yellow>线程A本应返回一个容器的最后一个元素，但是却抛出了异常，尽管抛出异常是符合 Vector 的规范。</font>

要想避免这种情况，就需要与同步容器类遵守相同的同步策略，即支持<a href="#客户端加锁">客户端加锁</a>。通过获得容器类的锁，使复合操作`getLast`和`deleteLast`成为原子操作

~~~java
public class SafeVectorHelpers {
    public static Object getLast(Vector list) {
        //客户端加锁
        synchronized (list) {
            int lastIndex = list.size() - 1;
            return list.get(lastIndex);
        }
    }

    public static void deleteLast(Vector list) {
        synchronized (list) {
            int lastIndex = list.size() - 1;
            list.remove(lastIndex);
        }
    }
}
~~~

这种风险在对 Vector 的元素进行迭代时仍然会出现，因此同样需要对迭代的代码段进行客户端加锁来避免这种风险。

~~~java
//在迭代期间获得锁，防止其他线程在迭代期间修改Vector
synchronized(vector){
    for(int i = 0; i < vector.size(); i++){
        doSomething(vector.get(i));
    }
}
~~~

然而这种方法降低了并发性，因为其他线程在迭代期间无法访问它。

**迭代器与ConcurrencyModificationEexeption**

无论是`for-each`循环还是直接迭代，对容器类进行迭代的标准方式都是使用`Iterator`，即迭代器

如果迭代器发现容器在迭代的过程中被修改，就会抛出 ConcurrencyModificationEexeption 异常

<font color=orange>与普通for循环一样，迭代器要想避免抛出异常，就必须在迭代过程中持有容器的锁</font>

~~~java
//可能会抛出ConcurrencyModificationEexeption异常
for (Object o : vector){
    doSomething(i);
}
~~~

**隐藏的迭代器**

虽然加锁可以防止迭代器抛出异常，实际情况是更加复杂的，因为在某些情况下，迭代器会隐藏起来

~~~java
public class HiddenIterator {
    @GuardedBy("this") 
    private final Set<Integer> set = new HashSet<Integer>();

    public synchronized void add(Integer i) {
        set.add(i);
    }

    public synchronized void remove(Integer i) {
        set.remove(i);
    }
	//当然真正的问题在于这个类不是线程安全的
    //如果对这个方法加上同步,那么仍然不会抛出异常
    public void addTenThings() {
        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            add(r.nextInt());
        }
        //这行代码可能会抛出异常
        //set会执行容器的toString()方法，这个方法会迭代容器
        System.out.println("DEBUG: added ten elements to " + set);
    }
}
~~~

容器的`hashCode`、`equals`、`containsAll`、`removeAll`和`retainAll`等方法都会对容器进行间接的迭代，因此都有可能抛出ConcurrencyModificationEexeption异常。

### 5.2 并发容器

同步容器的缺点：

将所有对容器状态的访问都串行化，以实现它们的线程安全性。这种方法的代价是严重降低了并发性，当多个线程竞争容器的锁时，吞吐量严重降低。

<font color=orange>并发容器是针对多个线程并发访问设计的。</font>例如：`ConcurrentHashMap`、`CopyOnWriteArrayList`

同时从 `JDK5` 开始，又提供了两种新的容器类型`Queue`、`BlockingQueue`。Queue上的操作不会阻塞，BlockingQueue 增加了可阻塞的插入和获取操作

<font color=orange>通过并发容器替代同步容器，可以极大地提高伸缩性并降低风险，不会抛出并发修改异常，提高并发性</font>

**5.2.1 ConcurrentHashMap**

`ConcurrentHashMap`也是一个基于散列的Map，但使用了完全不同的加锁策略来提供更高的并发性和伸缩性。

<font color=orange>不是将每个方法都在同一个锁上同步并使得每次只有一个线程访问容器，而是使用一种粒度更细的加锁机制来实现更大程度的共享，这种机制称为分段锁。</font>

在这种机制中，任意数量的读线程可以并发的访问Map，读线程和写线程可以并发地访问Map，并且一定数量的写进程也可以并发的修改Map。

在并发环境下将实现更高的吞吐量，而在单线程环境中只损失非常小的性能。

`ConcurrentHashMap`与其他并发容器一起增强了同步容器类：它们提供的迭代器不会抛出 `ConcurrentModifiedException` ，不需要在迭代过程中对容器加锁。可以容忍并发的修改，可以(但不保证)在迭代器被构造后将修改操作反映给容器。

对于常见的复合操作(先检查后执行 `putIfAbsent`、 `replace`等)，ConcurrentHashMap 都已经实现为原子操作。

**5.2.2 CopyOnWriteArrayList**

该容器用于替代同步的List，提供了更好的并发性能，在迭代期间不需要对容器进行加锁。

"写入时复制（Copy-On-Write）"容器的线程安全性在于：<font color=orange>只要正确发布了一个不变的对象，那么在访问该对象时就不在需要进一步的同步。在每次修改时，都会重建一个新的容器副本，从而实现可变性。</font>

容器的迭代器指向底层基础数组的引用，这个数组位于迭代器的初始位置。

由于数组不会被修改，在对其进行同步时只需要确保数组内容的可见性。因此，多个线程可以同时对这个容器进行迭代，而不会彼此干扰或者与修改容器的线程相互干扰。

"写入时复制"容器返回的迭代器不会抛出`ConcurrentModifiedException`，并且返回的元素与迭代器创建时的元素完全一致，而不必考虑之后修改该操作带来的影响。

<font color=orange>当修改容器时都会复制底层数组，当容器规模特别大时会需要一定的开销。仅当迭代操作远远多于修改操作时，才应该使用"写入时复制"容器</font>

~~~java
public class CopyOnWriteArrayList<E> implements List<E>{
    private transient volatile Object[] array;
    final transient Object lock = new Object();
    
    public boolean add(E e) {
        synchronized (lock) {
            Object[] elements = getArray();
            int len = elements.length;
            Object[] newElements = Arrays.copyOf(elements, len + 1);
            newElements[len] = e;
            //写入时复制
            setArray(newElements);
            return true;
        }
    }
   
    final void setArray(Object[] a) {
        array = a;
    }
}
~~~

<img src="https://s2.loli.net/2021/12/22/6HZMpANvUj4FybK.png" alt="image-20211222101425228" style="zoom:85%;float:left" />

### 5.3 阻塞队列和生产者-消费者模式

阻塞队列提供了可阻塞的`take`和`put`方法。

- 如果队列已经满了，那么`put`方法将阻塞直到有空间可以用。
- 如果队列为空，那么`take`方法将会阻塞直到有元素可用。
- 阻塞队列同样也提供了非阻塞的`offer`，如果数据项不能被添加到队列中，那么将返回一个失败状态。

阻塞队列支持生产者-消费者模式。比较典型的应用是线程池与工作队列的组合，在Executor任务执行框架中就体现了这种模式，这是第6章和第8章重点。

生产者和消费者模式将各自的代码解耦开来，但它们的行为仍通过共享工作队列间接地耦合在一起。

![image-20211222110502053](https://s2.loli.net/2021/12/22/AcaReCq5yMSPKpu.png)

`BlockingQueue`有多种实现，`LinkedBlockingQueue`、`ArrayBlockingQueue`、`PriorityBlockingQueue`以及`SynchronousQueue（同步队列）`

其中，`SynchronousQueue`不是一个真正的队列，不会为元素存储空间，它维护的是一组线程，这些线程等待着将元素移入或移出队列。

<font color=orange>降低了将数据从生产者移动到消费者的延迟，因此put和take会一直阻塞，直到有另一个线程已经准备好参与到交付过程中。</font>

这种实现方式相当于把文件直接交给同事，而其他实现方式是将文件放到同事的邮箱中并希望她尽快处理。

**示例**

~~~java
public class ProducerConsumer {
    //生产者：文件搜索
    static class FileCrawler implements Runnable {
        //共享队列
        private final BlockingQueue<File> fileQueue;
        private final FileFilter fileFilter;
        private final File root;

        public FileCrawler(BlockingQueue<File> fileQueue, final FileFilter fileFilter, File root) {
            this.fileQueue = fileQueue;
            this.root = root;
            this.fileFilter = new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || fileFilter.accept(f);
                }
            };
        }

        public void run() {
            try {
                crawl(root);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
		//搜索文件
        private void crawl(File root) throws InterruptedException {
            File[] entries = root.listFiles(fileFilter);
            if (entries != null) {
                for (File entry : entries)
                    if (entry.isDirectory())
                        crawl(entry);
                    else if (!alreadyIndexed(entry))
                        //将文件放入阻塞队列
                        fileQueue.put(entry);
            }
        }
    }
	//消费者：建立文件索引
    static class Indexer implements Runnable {
        private final BlockingQueue<File> queue;

        public Indexer(BlockingQueue<File> queue) {
            this.queue = queue;
        }

        public void run() {
            try {
                while (true)
                    //从阻塞队列中拿出文件
                    indexFile(queue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void indexFile(File file) {
            // 为文件建立索引
        };
    }

    private static final int BOUND = 10;
    private static final int N_CONSUMERS = Runtime.getRuntime().availableProcessors();
	//启动桌面搜索
    public static void startIndexing(File[] roots) {
        //初始化有界的队列
        BlockingQueue<File> queue = new LinkedBlockingQueue<File>(BOUND);
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return true;
            }
        };
		//启动生产者线程
        for (File root : roots)
            new Thread(new FileCrawler(queue, filter, root)).start();
		//启动消费者线程
        for (int i = 0; i < N_CONSUMERS; i++)
            new Thread(new Indexer(queue)).start();
    }
}
~~~

**串行线程封闭**

在`java.util.concurrent`包中实现的各种阻塞队列都包含了足够的同步机制，从而安全地将对象从生产者线程发布到消费者线程。

线程封闭对象只能由单个线程拥有，但可以通过安全发布对象来转移"所有权"，在转移所有权后，只有另一个线程能获得这个对象的访问权限，并且发布对象的线程不会再访问它。

### 5.4 同步工具类

同步工具类可以是任何一个对象，它根据自身的状态来协调线程的控制流，即<font color=orange>"它们封装了一些状态，这些状态将决定执行同步工具类的线程是继续执行还是等待，此外还提供了一些方法对状态进行操作，以及另一些方法用于高效地等待同步工具类进入到预期状态。"</font>

可以根据自身状态协调线程的控制流：

- 生产者消费者模式：阻塞队列（BlockingQueue）
- 并发流程控制：
  - 闭锁（CountDownLatch）
  - 栅栏（Barrier）
  - 信号量（Semaphore）
- 线程间的数据交换：交换者（Exchanger）

#### 5.4.1 闭锁 Latch

闭锁的作用相当于一扇门：在闭锁达到结束状态之前，这扇门一直是关闭的，并且没有任何线程能通过，当到达结束状态时，这扇门会打开并允许所有的线程通过。当闭锁达到结束状态后，<font color=orange>将不会再改变状态</font>，因此这扇门会永远保持打开状态。

闭锁可以确保某些活动直到其他活动都完成后才继续执行：

- 确保某个计算在其需要的所有资源都初始化之后才能继续执行
- 确保某个服务在其依赖的所有其他服务都启动后才启动
- 等待直到某个操作的所有参与者都就绪再继续执行

~~~java
//CountDownLatch是闭锁的一个实现
public class CountDownLatch {
    //内部类
    private static final class Sync extends AbstractQueuedSynchronizer {
        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            // 递减Count,直到Count为0
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c - 1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
    }
    //状态 sync
    private final Sync sync;
    //表示某个事件已经发生，计数器递减
    public void countDown() {
        sync.releaseShared(1);
    }
    //等待计数器到达0,这表示所有需要等待的事件都已经发生。如果计数器的值非0，那么await方法会一直阻塞直到计数器为0
    public void await() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
    }
}
~~~

下面这个案例是使用闭锁来实现计时测试

~~~java
public class TestHarness {
    public long timeTasks(int nThreads, final Runnable task) throws InterruptedException {
        //两个闭锁：起始门和结束门
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch endGate = new CountDownLatch(nThreads);

        for (int i = 0; i < nThreads; i++) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        //所有线程都阻塞在起始门
                        startGate.await();
                        try {
                            //执行任务
                            task.run();
                        } finally {
                            //递减计数器
                            endGate.countDown();
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
            };
            t.start();
        }
        //等待线程都已经启动并且都阻塞在启动门上
        Thread.sleep(1000);
		//计时
        long start = System.nanoTime();
        //放开起始门
        startGate.countDown();
        //所有线程都阻塞在结束门上，直到所有线程都已经执行过任务
        endGate.await();
        long end = System.nanoTime();
        return end - start;
    }
}
~~~

#### 5.4.2 FutureTask

`FutureTask`也可以用作闭锁。需要实现Callable接口，执行call()方法返回结果。

~~~java
@FunctionalInterface
public interface Callable<V> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     */
    V call() throws Exception;
}
~~~

`FutureTask`的`get()`的行为取决于任务的状态，如果任务已经完成，会立即返回结果，否则将阻塞直到任务进入完成状态，然后返回结果或者抛出异常。

使用FutureTask可以用来执行一个计算结果，并且计算结果在稍微使用。

~~~java
public class Preloader {
    private final FutureTask<ProductInfo> future = new FutureTask<ProductInfo>(
        new Callable<ProductInfo>() {
            public ProductInfo call() throws DataLoadException {
                //执行高开销的计算
                return loadProductInfo();
            }
        });
    //新建一个线程,专门用来计算任务
    private final Thread thread = new Thread(future);

    public void start() { thread.start(); }

    public ProductInfo get() throws DataLoadException, InterruptedException {
        try {
            //返回结果或抛出异常
            return future.get();
        } catch (ExecutionException e) {
            //对异常进行分类检查
            Throwable cause = e.getCause();
            if (cause instanceof DataLoadException)
                throw (DataLoadException) cause;
            else
                throw LaunderThrowable.launderThrowable(cause);
        }
    }

    interface ProductInfo {
    }
}

class DataLoadException extends Exception { }
~~~

#### 5.4.3 信号量 Semaphore

计数信号量用来控制同时访问特定资源的线程数量(线程池)，还可以对容器加边界。

Semaphore中管理着一组虚拟的许可(permit)。许可的数量可以通过构造函数传递。

在执行操作时首先调用`acquire()`获得许可，并在使用以后释放`release()`许可。如果没有许可，那么`acquire`将阻塞直到有许可（或者被中断或超时）

下面这段代码演示了如何使用Semaphore将容器变为有界容器。

~~~java
//使用信号量给容器具备有界性
public class BoundedHashSet <T> {
    private final Set<T> set;
    //计数信号量
    private final Semaphore sem;

    public BoundedHashSet(int bound) {
        this.set = Collections.synchronizedSet(new HashSet<T>());
        //在这个案例中：许可量等于容量
        sem = new Semaphore(bound);
    }
	
    public boolean add(T o) throws InterruptedException {
        //获取许可,这个方法实现了同步
        sem.acquire();
        boolean wasAdded = false;
        try {
            wasAdded = set.add(o);
            return wasAdded;
        } finally {
            //没添加成功，释放许可
            if (!wasAdded)
                sem.release();
        }
    }

    public boolean remove(Object o) {
        boolean wasRemoved = set.remove(o);
        //移除成功，释放许可
        if (wasRemoved)
            sem.release();
        return wasRemoved;
    }
}
~~~

#### 5.4.4 栅栏 Barrier

栅栏类似于闭锁，它能阻塞一组线程直到某个事件发生。

栅栏用于等待其他线程，闭锁用于等待事件

当线程到达栅栏位置时将调用`await()`方法，这个方法将阻塞直到所有线程都到达栅栏位置。如果所有线程都达到栅栏位置，那么栅栏将打开，此时所有线程都将释放，<font color=orange>而栅栏将被重置以便下次使用。这是它与闭锁的另一个区别。</font>

~~~java
// parites:需要等待的线程数
// barrierAction:当栅栏通过时的动作
public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException();
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
}
~~~

Demo

~~~java
public class CyclicBarrierDemo2 {
    //创建栅栏,等待两个线程
    static CyclicBarrier barrier = new CyclicBarrier(2, new After());

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                System.out.println("In thread");
                try {
                    //在栅栏处等待
                    barrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        System.out.println("In main");
        try {
            //在栅栏处等待
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finish.");
    }
	
    static class After implements Runnable {
        @Override
        public void run() {
            System.out.println("All reach barrier.");
        }
    }
}
~~~

## 第六章 线程池使用

### 6.1 任务及调度策略

任务：抽象且离散的工作单元

理想状态下，各个任务之间是相互独立的：一个任务不依赖于其他任务的状态，因此这些独立的任务可以通过并行的方式执行。

大多数服务器应用提将独立的客户请求作为任务边界，实现任务的独立性。

<img src="https://s2.loli.net/2021/12/24/kMrS8WajgYPU5nb.png" alt="image-20211224133515113" style="zoom:80%;float:left" />

应用程序有三种策略来调度任务：

|           名称           |                             特点                             |
| :----------------------: | :----------------------------------------------------------: |
|   单线程串行的执行任务   | 一个任务必须等待前一个任务结束后才能执行，响应慢吞，吐量低。有I/O阻塞时，CPU空闲得不到利用。 |
| 为每个任务创建一个新线程 | 不断的创建线程会消耗大量的资源，当线程数远大于CPU时，会因为大量的竞争产生其他性能消耗 |
|          线程池          | 重用现有的线程，节省线程生命周期产生的消耗，简化了线程的管理工作 |

<img src="https://s2.loli.net/2021/12/24/MqC31zxfVY8vUkc.png" alt="image-20211224140523524" style="zoom:70%;float:left" />

### 6.2 Exeutor框架

#### 6.2.1 Executor 接口

在Java中，任务执行的主要抽象是`Executor`而不是`Thread`

任务使用哪种调度策略来执行取决于`Executor`接口的实现类。换句话说，单线程、多线程还是线程池都可以通过实现Executor接口来定义。

~~~java
//任务执行的接口
public interface Executor {
    //执行任务，会抛出RejectExecutionException
    void execute(Runnable command);
}
~~~

使用Executor实现线程池

~~~java
class TaskExecutinoWebServer{
    private static final int NTHREADS  = 100;
	//创建线程池的Executor
	private static final Executor exec = Executors.newFixedThreadPool(NTHREADS);
	public static void main(String[] args){
        ServerSocket server = new ServerSocket(80);
        while(true){
            Socket conn = server.accept();
            Runnable task = new Runnable(){
                public void run(){
                    handleRequest(task);
                }
            };
            //将任务提交到线程池的工作队列中
            exec.execute(task);
        }
    }
}	
~~~

<font color=orange>使用Executor，将请求任务的提交与任务的实际执行解耦开来，生产者与消费者模式</font>

- 将任务提交给工作队列
- 线程池中的线程从工作队列中取出任务
- 执行任务
- 线程在执行完毕后返回线程池

![image-20211224142933976](https://s2.loli.net/2021/12/24/lOExekaUNQfwvID.png)

可以修改Executor的实现，改变任务的处理方式。

~~~java
//为每个任务创建新线程的Executor
public class ThreadPerTaskExecutor implements Executor {
    public void execute(Runnable r) {
        new Thread(r).start();
    };
}
//单线程处理任务的Executor
public class WithinThreadExecutor implements Executor {
    public void execute(Runnable r) {
        r.run();
    };
}
~~~

#### 6.2.2 Future 接口

`Executor`使用`Runnable`作为其基本的任务表示形式，但有一个很大的局限性：无法返回计算结果或者抛出异常。

~~~java
public interface Runnable {
    public abstract void run();
}
~~~

现实中很多任务都是存在延迟的计算的  --- 执行数据库查询、计算某个复杂的功能等。

`Callable`是一个可以返回结果的抽象任务接口

~~~java
public interface Callable<V> {
    V call() throws Exception;
}
~~~

`Runnable`和`Callable`描述的都是抽象的计算任务。对于`Executor`执行的任务，会有四个生命周期：创建、提交、完成、结束。

- 已提交但还未开始的任务，例如在消息队列等候的任务，可以取消
- 正在执行的任务，响应中断才能取消
- 取消已完成的任务不会有影响

`Future`表示一个任务的生命周期，提供相应的方法判断任务是否已经完成或取消，以及获取任务的结果

~~~java
public interface Future<V> {
    //取消任务,参数为true则取消任务,false则继续执行任务至完成状态
    boolean cancel(boolean mayInterruptIfRunning);
  	//是否取消
    boolean isCancelled();
    //是否完成
    boolean isDone();
    //获取任务的结果,如果任务没有完成则阻塞等待，任务完成则返回结果或抛出异常
    //三种异常：
    //InterruptException：线程在wait时被中断
    //CancellationException：任务被取消
    //ExecutionException：任务执行过程中出错
    V get() throws InterruptedException, ExecutionException;
	//在指定时间内获取任务的结果
    V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
}
~~~

<img src="https://s2.loli.net/2021/12/24/ueID2lSM537xVTW.png" alt="image-20211224150834201" style="zoom:90%;float:left" />

`Runnable`和`Callable`转换成`Future`

~~~java
protected <T> RunnableFuture<I> newTaskFor(Callable<T> task){
    return new FutureTask<T>(task);
}
protected <T> RunnableFuture<I> newTaskFor(Runnable<T> task){
    return new FutureTask<T>(task);
}
~~~

#### 6.2.3 ExecutorService 接口

`Executor`的实现通常会创建线程来执行任务，但JVM只有在所有(非守护)线程全部终止后才会退出。如果`Executor`无法正确地关闭，那么JVM就无法正常结束。

`ExectutorService`扩展了`Executor`接口，提供了管理生命周期的方法，并且也增加了便于任务提交的方法。

~~~java
public interface ExecutorService extends Executor {
    //执行平缓的关闭过程：不再接受新任务，同时等待已提交的任务完成，包括队列中等待的任务
    void shutdown();
	//执行粗暴的关闭过程：尝试取消所有运行中的任务，并且不再启动队列中等候的任务
    List<Runnable> shutdownNow();
	//查询Executor是否已经关闭
    boolean isShutdown();
	//轮询在Executor关闭后所有任务是否已经完成
    boolean isTerminated();
	//阻塞直到所有任务已经进入完成状态
    boolean awaitTermination(long timeout, TimeUnit unit)throws InterruptedException;
	//提交Callable任务，返回Future
    <T> Future<T> submit(Callable<T> task);
	//提交Runnable任务，返回Future
    Future<?> submit(Runnable task);
	//提交一组Callabel任务，返回一组Future
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException;
	//提交一组Runnable任务,返回一组Future
    <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException;
}
~~~

<img src="https://s2.loli.net/2021/12/24/f1nyGkJzOxpuSEm.png" alt="image-20211224152925396" style="zoom:80%;float:left" />

抛出`RejectExecutionExcepion`的原因：

1. 工作队列已满，饱和策略
2. 任务被提交到已关闭的Executor

#### 6.2.4 CompletionService 接口

~~~java
public interface CompletionService<V> {
    //提交callable任务
    Future<V> submit(Callable<V> task);
	//提交runnable任务
    Future<V> submit(Runnable task, V result);
	//从阻塞队列中提取future
    Future<V> take() throws InterruptedException;
	//从阻塞队列中提取future，非阻塞
    Future<V> poll();
	//限定时间内提取future
    Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;
}
~~~

`CompletionService`将`Executor`和`BlockingQueue`的功能融合到一起，将任务包装成FutureTask的子类`QueueingFuture`放进阻塞队列。

<img src="C:\Users\PSJ\AppData\Roaming\Typora\typora-user-images\image-20211224164202009.png" alt="image-20211224164202009" style="zoom:80%;float:left" />

`CompletionService`的实现类为`ExecutorCompletionService`，该实现类还包含了一个内部类`QueueingFuture`

~~~java
public class ExecutorCompletionService<V> implements CompletionService<V> {
    private final Executor executor;
    private final AbstractExecutorService aes;
    private final BlockingQueue<Future<V>> completionQueue;

    //内部类QueueingFuture
    private static class QueueingFuture<V> extends FutureTask<Void> {
        QueueingFuture(RunnableFuture<V> task, BlockingQueue<Future<V>> completionQueue) {
            super(task, null);
            this.task = task;
            this.completionQueue = completionQueue;
        }
        private final Future<V> task;
        private final BlockingQueue<Future<V>> completionQueue;
        //初始化后将任务添加进队列
        protected void done() { completionQueue.add(task); }
    }
    //初始化CompletionService
    public ExecutorCompletionService(Executor executor) {
        if (executor == null) throw new NullPointerException();
        this.executor = executor;
        this.aes = (executor instanceof AbstractExecutorService) ? (AbstractExecutorService) executor : null;
        this.completionQueue = new LinkedBlockingQueue<Future<V>>();
    }
	//提交任务
    public Future<V> submit(Callable<V> task) {
        if (task == null) throw new NullPointerException();
        //将task转换成future
        RunnableFuture<V> f = newTaskFor(task);
        //将任务添加进队列
        executor.execute(new QueueingFuture<V>(f, completionQueue));
        return f;
    }
	//阻塞提取future
    public Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }
	//非阻塞提取future
    public Future<V> poll() {
        return completionQueue.poll();
    }
    ...
}
~~~

### 6.3 线程池

创建线程池的方式

- `Executors`的静态工厂方法
- 创建`ThreadPoolExecutor`

