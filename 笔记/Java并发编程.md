Java并发编程实战

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
- 对于含有final域的对象，初始化安全性可以防止对对象的初始引用会被重排序到构造过程之前。

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
public static Holder holder;
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

<font color=orange>并发容器是针对多个线程并发访问设计的。</font>例如：`ConcurrentHashMap`、`CopyOnWriteArrayList`、`ConcurrentLinkedList`

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

> 同步工具类基于AQS(Abstract Queued Synchronizer)构建

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

|           名称           |                             特点                             |
| :----------------------: | :----------------------------------------------------------: |
|    newFixedThreadPool    | 创建固定长度的线程池，提交一个任务时创建一个线程，直到达到线程池的最大数量，这时线程池的规模不再变化 |
|   newCachedThreadPool    | 线程池规模超过处理需求时，会回收空闲的线程。当需求增加时，则可以添加新的线程，规模不存在任何限制 |
| newSingledThreadExecutor | 创建单个工作线程来执行任务，如果这个线程异常结束，会创建另一个线程代替 |
|  newScheduledThreadPool  |       创建固定长度的线程池，以延迟和定时的方式执行任务       |

#### 6.3.1 创建线程池

**Executors:**

Executor 和 ExecutorService 的工具类

~~~java
// 提供静态方法创建新线程
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
}
public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
}
~~~

**ThreadPoolExecutor:**

也可以通过ThreadPoolExecutor的构造函数来实例化一个线程池，根据需求自己定制。

~~~java
public ThreadPoolExecutor(int corePoolSize,                               //基本大小
                          int maximumPoolSize,                            //最大大小
                          long keepAliveTime,                             //当线程数量大于最大值时，多余线程等待任务的存活时间
                          TimeUnit unit,								  //存活时间的单位
                          BlockingQueue<Runnable> workQueue,              //工作队列
                          ThreadFactory threadFactory,                    //线程池工厂
                          RejectedExecutionHandler handler)               //饱和策略
						  {...}                                           
~~~

**参数介绍：**

BlockingQueue<Runnable> workQueue:

允许提供一个阻塞队列来保存等待执行的任务，排队方法有三种：

|   名称   |                             特点                             |
| :------: | :----------------------------------------------------------: |
| 有界队列 |        避免资源耗尽的情况发生，队列满后将触发饱和策略        |
| 无界队列 | 如果任务快速的到达，并且超过了线程池处理的速度，那么队列将无限增加 |
| 同步移交 |   直接将任务从生产者线程移交给工作线程，拥有更好的排队性能   |

#### 6.3.2 饱和策略

有界队列被填满后，饱和策略开始发挥作用。可以通过`setRejectionExecutionHandler`处理：

- AbortPolicy：抛出异常，调用者可以自己处理该异常
- DiscardPolicy：抛弃该任务
- DiscardOldestPolicy：抛弃下一个将要执行的任务，然后尝试重新提交新的任务
- CallerRunsPolicy：不会抛出异常，也不会抛弃任务，而是将任务回退到调用者，从而降低新任务的流量。将在调用execute的线程中执行任务

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112271020532.png" alt="image-20211227102033454" style="float:left;zoom:80%;" />

~~~java
    //ThreadPoolExecutor的静态内部类
	public static class CallerRunsPolicy implements RejectedExecutionHandler {
        public CallerRunsPolicy() { }
        //在调用者线程执行任务
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
	}

	public static class AbortPolicy implements RejectedExecutionHandler {
        public AbortPolicy() { }
		//抛出异常
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +" rejected from " + e.toString());
        }
	}

	public static class DiscardPolicy implements RejectedExecutionHandler {
        public DiscardPolicy() { }
		//抛弃任务
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {}
	}


    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        public DiscardOldestPolicy() { }
		//抛弃下一个将要执行的任务，然后重新执行新任务
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }
~~~

#### 6.3.3 线程工厂

当线程池需要创建一个线程时，通过线程工厂方法创建。

默认的线程工厂方法将创建一个新的、非守护的线程，并且不包含特殊的配置信息

可以自己创建线程工厂自定义创建的新线程

![image-20211227111118572](https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112271111636.png)

~~~java
public interface ThreadFactory {
    Thread newThread(Runnable r);
}
~~~

~~~java
//创建自己的线程工厂
public class MyThreadFactory implements ThreadFactory {
    private String name;

    public MyThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new MyThread(r, name);
    }
}
~~~

~~~java
//线程类
public class MyThread extends Thread {

    public MyThread(Runnable r, String name) {
        super(r, name);
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
        //执行Runnable的run方法
        super.run();
    }

    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor
            (1,1,2000, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>(), new MyThreadFactory("loubei's ThreadFactory"));
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("线程开始工作");
                    long start = System.currentTimeMillis();
                    Thread.sleep(10000);
                    long end   = System.currentTimeMillis();
                    System.out.println("工作了：" + (end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        threadPoolExecutor.execute(r);
    }
}
~~~

**执行结果**

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112271059671.png" alt="image-20211227105903614" style="float:left" />

#### 6.3.4 线程死锁

线程饥饿死锁（Thread Starvation Deadlock）:

线程中的任务需要无限期等待一些必须由池中阻塞队列的其他任务提供的资源或条件才能继续运行。如果线程数量不够的话，可能会发生线程死锁。

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112270927037.png" alt="image-20211227092706888" style="float:left" />

产生死锁的原因:

- Task 0需要等待Task 1和Task 2的结果才能结束对线程的占用
- Task 1和Task 2需要线程执行完Task 0后才能轮到自己

解决方法：增加线程的数量

## 第七章 活跃性

活跃性危险：

- 死锁
- 饥饿
- 活锁

### 7.1 死锁

每个人都拥有其他人需要的资源，同时等待其他人拥有的资源，并且每个人在获得所有需要的资源之前不会放弃自己已经拥有的资源

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112290919082.png" alt="image-20211229091936950" style="float:left" />

数据库服务器检测到死锁时，会选择一个牺牲者并放弃这个事务，作为牺牲者的事务会释放它拥有的资源，从而使其他事务能够继续进行。

Java线程发生死锁时，唯一恢复程序的方式是终止并重启它。

#### 7.1.1 锁顺序死锁

两个线程试图以不同的顺序获得相同的锁。

~~~java
	private final Object left = new Object();
    private final Object right = new Object();

    public void leftRight() {
        //线程A获得left锁后发生线程的切换
        synchronized (left) {
            synchronized (right) {
                doSomething();
            }
        }
    }

    public void rightLeft() {
        //线程B获得right锁，但无法获得left锁
        synchronized (right) {
            synchronized (left) {
                doSomethingElse();
            }
        }
    }
~~~

#### 7.1.2 动态的锁顺序死锁

将多个锁作为方法的参数传入，即便所有线程在方法内部都是按照相同的顺序获得锁，但由于参数顺序取决于外部输入，因此锁的顺序仍然可能不同最终导致死锁。

~~~java
A:transferMoney(myAccount, yourAccount, 10);
B:transferMoney(yourAccount, myAccount, 20);

public void transferMoney(final Account myAccount, final Account yourAccount, final int money){
    synchronized(myAccount){
        synchronized(yourAccount){
            ...
        }
    }
}
~~~

<font color=pink>解决这个问题，必须定义锁的顺序，并在整个应用程序中都按照这个顺序来获取锁。</font>

使用`System.identifyHashCode`来定义锁的顺序

~~~java
	private static final Object tieLock = new Object();

    public void transferMoney(final Account fromAcct,final Account toAcct,final DollarAmount amount)throws InsufficientFundsException {
        //内部类
        class Helper {
            public void transfer() throws InsufficientFundsException {
                if (fromAcct.getBalance().compareTo(amount) < 0)
                    throw new InsufficientFundsException();
                else {
                    fromAcct.debit(amount);
                    toAcct.credit(amount);
                }
            }
        }
        //根据第一个参数计算fromHash
        int fromHash = System.identityHashCode(fromAcct);
        //根据第二个参数计算toHash
        int toHash = System.identityHashCode(toAcct);
		//根据不同的哈希值大小比较，匹配不同的锁顺序
        if (fromHash < toHash) {
            synchronized (fromAcct) {
                synchronized (toAcct) {
                    new Helper().transfer();
                }
            }
        } 
        else if (fromHash > toHash) {
            synchronized (toAcct) {
                synchronized (fromAcct) {
                    new Helper().transfer();
                }
            }
        }
        //哈希值相同的情况，使用"加时赛锁"，确保只有一个线程可以获得未知顺序的锁
        else {
            synchronized (tieLock) {
                synchronized (fromAcct) {
                    synchronized (toAcct) {
                        new Helper().transfer();
                    }
                }
            }
        }
    }
~~~

#### 7.1.3 协作对象之间的死锁

在持有锁时调用某个外部方法，将可能发生活跃性问题。这个外部方法中可能会获取其他锁(这可能会产生死锁)。

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112291054474.png" alt="image-20211229105446406" style="float:left" />

**开放调用**：在调用某个方法时不需要持有锁。

#### 7.1.4 避免死锁

- 开放调用
- 显式使用Lock类中的定时`tryLock`功能来代替内置锁机制。(书本第十三章)使用内置锁时，只要没有获得锁，就会永远等待下去，而显式锁则可以指定一个超时时限(Timeout)，在等待超过时间后`tryLock`会返回一个错误信息。如果获取锁时超时，那么可以释放这个锁，然后后退并在一段时间后重试，从而消除了死锁发生的条件。

### 7.2 饥饿

- 线程无法访问它所需要的资源，导致一直等待下去。（线程饥饿死锁）
- 最常见的资源就是**CPU时钟周期**，如果对线程的优先级使用不当，或者在持有锁时执行无法结束的结构，那么可能会导致饥饿

### 7.3 活锁

> 单线程的死循环是活锁的表现形式之一

活锁不会阻塞线程，但也无法继续执行，因为线程将不断重复执行相同的操作。

活锁通常发生在处理事务消息的应用程序中，消息处理器会将无法处理成功的事务回滚到队列的开头，因为事务被放回到队列开头，因此处理器会反复调用。

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112291102911.png" alt="image-20211229110227846" style="float:left" />

在多个相互协作的线程都对彼此进行响应从而修改彼此的状态，使得任何线程都无法继续执行时，就发生了活锁。

解决活锁问题，需要在重试机制中加入随机性。

## 第八章 性能与可伸缩性

- 吞吐量：一组并发任务中已完成任务所占的比例
- 响应性：请求从发出到完成的时间
- 可伸缩性：在增加更多资源的情况下，吞吐量的提升情况

### 8.1 Amdahl定律

> Amdahl定律描述的是：在增加计算资源的情况下，程序在理论上能够实现的最高加速比取决于可并行组件与串行组件的比重


$$
SpeedUp <= \frac{1}{F+\frac{(1-F)}{N}}  (F是必须串行执行的部分,N是处理器个数)
$$
当处理器个数N趋近于无穷大时，串行执行比例越少，性能提升的越多，即应用程序伸缩性越好

### 8.2 线程引入的性能开销

|    名称    |                             原因                             |
| :--------: | :----------------------------------------------------------: |
| 上下文切换 | 线程阻塞或者发生I/O时，操作系统会将当前线程挂起并将状态保存进TCB，接着再把下一个可执行线程的状态加载进内存 |
|  内存同步  | 在`synchronized`和`volatile`的可见性保证中，会使用一些特殊的指令，即内存栅栏(Memory Barrier)，直接使用主内存、刷新本地内存,同时会抑制重排序 |
|    阻塞    | JVM在线程阻塞时通常选择将线程挂起，会产生两次上下文切换(阻塞时将线程切换出去，有资源时将线程切换回来) |

JVM实现的两种阻塞行为：

1. 通过操作系统将阻塞的线程挂起 【适合等待时间长的】
2. 自旋等待(Spin-Waiting:通过循环不断的等待地尝试获取锁，直到成功) 【适合等待时间短的】

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201011656184.png" alt="image-20220101165659111" style="zoom:88%;float:left" />

### 8.3 减少锁的竞争

1. 减少锁的持有时间
2. 减低锁的请求频率
3. 使用带有协调机制的独占锁，这些机制允许更高的并发性

> 有两种因素影响在锁上发生竞争的可能性：锁的请求频率，锁的持有时间.

#### 8.3.1 缩小锁的范围

将一些与锁无关的代码移除同步代码块，尤其是那些开销比较大的操作，以及可能被阻塞的操作，例如I/O操作。

`这种方式可以尽可能地缩小锁的持有时间`

~~~java
public class BetterAttributeStore {
    @GuardedBy("this") 
    private final Map<String, String> attributes = new HashMap<String, String>();
    
    //直接锁住整个方法
    public synchronized boolean userLocationMatches(String name, String regexp) {
        String key = "users." + name + ".location";
        String location = attributes.get(key);
        if (location == null)
            return false;
        else
            return Pattern.matches(regexp, location);
    }
	//缩小锁住的同步块范围
    public boolean userLocationMatches(String name, String regexp) {
        String key = "users." + name + ".location";
        String location;
        synchronized (this) {
            location = attributes.get(key);
        }
        if (location == null)
            return false;
        else
            return Pattern.matches(regexp, location);
    }
}
~~~

#### 8.3.2 减小锁的粒度

> 通过锁分段和锁分解，将采用多个相互独立的锁来保护独立的状态变量，从而改变这些变量之前由单个锁来保护的情况

`这种方式可以降低线程请求锁的频率`

锁分解：将一个竞争激烈的锁分解成两个相互独立的锁

锁分段：将锁分解技术进一步扩展为对一组独立对象上的锁进行分解

~~~java
@ThreadSafe
public class ServerStatusBeforeSplit {
    @GuardedBy("this") 
    public final Set<String> users;
    @GuardedBy("this") 
    public final Set<String> queries;

    public ServerStatusBeforeSplit() {
        users = new HashSet<String>();
        queries = new HashSet<String>();
    }
	//使用类的内置锁完成同步
    public synchronized void addUser(String u) {
        users.add(u);
    }

    public synchronized void addQuery(String q) {
        queries.add(q);
    }

    public synchronized void removeUser(String u) {
        users.remove(u);
    }

    public synchronized void removeQuery(String q) {
        queries.remove(q);
    }
}
~~~

使用锁分解技术后：

~~~java
@ThreadSafe
public class ServerStatusAfterSplit {
    @GuardedBy("users") 
    public final Set<String> users;
    @GuardedBy("users") 
    public final Set<String> queries;

    public ServerStatusAfterSplit() {
        users = new HashSet<String>();
        queries = new HashSet<String>();
    }

    public void addUser(String u) {
        synchronized (users) {
            users.add(u);
        }
    }

    public void addQuery(String q) {
        synchronized (queries) {
            queries.add(q);
        }
    }

    public void removeUser(String u) {
        synchronized (users) {
            users.remove(u);
        }
    }

    public void removeQuery(String q) {
        synchronized (users) {
            queries.remove(q);
        }
    }
}
~~~

`ConcurrHashMap`使用了锁分段技术

下面是基于散列的HashMap实现

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201021951510.png" alt="image-20220102195116381" style="float:left" />

~~~java
@ThreadSafe
public class StripedMap {
    private static final int N_LOCKS = 16;
    private final Node[] buckets;
    private final Object[] locks;

    private static class Node {
        Node next;
        Object key;
        Object value;
    }

    public StripedMap(int numBuckets) {
        buckets = new Node[numBuckets];
        locks = new Object[N_LOCKS];
        for (int i = 0; i < N_LOCKS; i++)
            locks[i] = new Object();
    }
	//计算哈希值
    private final int hash(Object key) {
        return Math.abs(key.hashCode() % buckets.length);
    }

    public Object get(Object key) {
        int hash = hash(key);
        //使用locks[hash % N_LOCKS]作为锁
        synchronized (locks[hash % N_LOCKS]) {
            for (Node m = buckets[hash]; m != null; m = m.next)
                if (m.key.equals(key))
                    return m.value;
        }
        return null;
    }
	//clear方法要求获得所有的锁
    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            synchronized (locks[i % N_LOCKS]) {
                buckets[i] = null;
            }
        }
    }
}
~~~

#### 8.3.3 热点域

> 热点域：将一些反复计算的结果缓存起来

在实现HashMap时，需要考虑在size()方法中计算Map中的元素个数。除了每次都遍历外，一种常见的优化方法是，在插入和移除元素时，更新一个计数器。

但是每次修改这个map时，都需要独占锁来更新这个共享的计数器。这会重新导致使用独占锁时存在的可伸缩性问题。

<font color=pink>解决方法是对每个bucket都设置一个计数器，先计算每个bucket的大小，最后累加得到总的size。</font>

在性能方面，通过每个分段的锁来维护这个值。

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201032222501.png" alt="image-20220103222212419" style="zoom:80%;float:left" />

#### 8.3.4 替代独占锁的方法

放弃使用独占锁的方式来降低竞争锁的影响

1. 使用并发容器
2. 读-写锁 （第13章）
3. 不可变对象
4. 原子变量（第15章）

## 第九章 显式锁

> Lock接口提供了一种更灵活的锁机制，用于补充内置锁某些功能的缺陷，例如打断等待锁的线程
>
> ReetrantLock是Lock接口的实现类，"重入锁"

常见的代码结构

~~~java
// 重入锁	
Lock lock = new ReetrantLock();
// 获取锁
lock.lock();
try{
// 更新对象状态    
}
// 释放锁
finally{
    lock.unlock();
}
~~~

### 9.1 Lock接口

#### 9.1.1 lock()

获取锁，如果锁不是有效的，则线程进入阻塞状态直到获取锁

~~~java
void lock();
~~~

#### 9.1.2 unlock()

~~~java
void unlock(); // 释放锁
~~~

#### 9.1.2 tryLock()

> 使用可定时的或可轮询的锁获取方式，可以有效避免死锁的发生

~~~java
Lock lock1 = new ReetrantLock();
Lock lock2 = new ReetrantLock();
// 如果在获得lock1的情况下，lock2被其他线程持有，那么持有lock1的线程会释放lock1，然后重新尝试获取所有锁
while(true){
    if(lock1.tryLock()){
        try{
            if(lock2.tryLock()){
                try{
                    ...
                }finally{
                    lock2.unlock();
                }
            }
        }
        finally{
            lock1.unlock();
        }
	}
}
~~~

<font color=orange>可轮询的锁获取模式</font>

~~~java
boolean tryLock(); // 如果锁是有效的，tryLock()会立即返回true，否则返回false
~~~

<font color=orange>可定时的锁获取方式</font>

~~~java
boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
~~~

如果锁是有效的，则立即返回true，

如果锁不是有效的，那么线程陷入阻塞状态，直到发生以下三种情况：

- 在指定时间内获得了锁，返回true
- 其他线程打断了当前线程，抛出`InterruptedException`
- 在指定时间内没有获取锁，返回false

#### 9.1.3 lockInterruptibly()

~~~java
void lockInterruptibly() throws InterruptedException;
~~~

如果锁是有效的，则立即返回

如果锁不是有效的，那么线程会陷入阻塞状态，直到发生以下两种情况：

- 当前线程获得了锁，返回
- 其他线程打断了当前线程，抛出`InterruptedException`

~~~java
Lock lock = new ReetrantLock();
lock.lockInterruptibly();
try{
    ...
}
finally{
    lock.unlock();
}
~~~

### 9.2 公平性与非公平性

> 在ReetrantLock的构造函数中，可以选择锁的公平性与非公平性

- 公平性：有其余线程在等待锁时，当前线程放入锁的等待队列中
- 非公平性：如果请求锁的同时，锁的状态变为可用，则当前线程跳过队列中其他等待的线程获得这个锁。如果其他线程持有锁，则放入等待队列

~~~java
// 默认的构造方法为非公平锁
public ReentrantLock() {
     sync = new NonfairSync();
}
// 带参的构造方法，自己选择锁的公平性
public ReentrantLock(boolean fair) {
     sync = fair ? new FairSync() : new NonfairSync();
}
~~~

在锁竞争激烈的情况下，非公平锁的性能高于公平锁：<font color=orange>恢复被挂起的线程与该线程开始真正运行之间存在着严重的时间延迟(上下文切换),但锁的非公平能让一个线程在"某个时机"直接插队获得该锁,提高了吞吐量。</font>

### 9.3 读写锁

> ReetrantLock实现了标准的互斥锁，每次最多只有一个线程能持有ReetrantLock，这是一种保守的策略。
>
> 虽然避免了读/写冲突，写/写冲突，但也避免了读/读冲突

在许多情况下，大多数访问操作都是读操作，此时如果能放宽加锁需求，允许多个执行读操作的线程同时访问数据结构，那么将提升程序的性能。

读写锁：一个资源可以被多个读操作访问，或者被一个写操作访问，但两者不能同时进行。

~~~java
public interface ReadWriteLock {
    // 返回读锁
    Lock readLock();
	// 返回写锁
    Lock writeLock();
}
~~~

~~~java
private final ReadWriteLock lock = new ReetrantReadWriteLock();
private final Map<K, V> map;
private final Lock readLock = lock.readLock();
private final lock writeLock = lock.writeLock();
// 写操作
public V put(K key, V value){
    // 写锁
    writeLock.lock()；
    try{
        return map.put(key, value);
    }
    finally{
        writeLock.unlock();
    }
}
// 读操作
public V get(K key){
    // 读锁
    readLock.lock();
    try{
        return map.get(key);
    }
    finally{
        readLock.unlock();
    }
}
~~~

### 9.4 注意

在ReetrantLock与ReetrantReadWriteLock的具体实现中都用到了`AQS`的知识，这在后面会进行介绍。











## 第十章 AQS

### 10.1 属性

AQS的属性

~~~java
	// 头结点：代表当前持有锁的线程
	private transient volatile Node head;
	// 阻塞的尾节点
    private transient volatile Node tail;
    // 当前锁的状态，0代表没有被占用，大于1代表可重入
	private volatile int state;
	// 独占模式下拥有锁的线程
    private transient Thread exclusiveOwnerThread;
~~~

静态内部类Node

~~~java
static final class Node {
    	// 表示当前节点处于共享模式
        static final Node SHARED = new Node();
    	// 标识当前节点在独占模式
        static final Node EXCLUSIVE = null;
		//下面几个是waitstatus的状态
        // 表示线程取消了争锁
        static final int CANCELLED =  1;
		// 表示node的后继节点需要被唤醒
        static final int SIGNAL    = -1;
		// 条件队列模式
        static final int CONDITION = -2;
        // ...
        static final int PROPAGATE = -3;
    ===================================================
		// 当前节点的等待状态
        volatile int waitStatus;
		// 前驱节点
        volatile Node prev;
		// 后驱节点
        volatile Node next;
       	// 当前线程
        volatile Thread thread;
		// 在条件队列上等待的下一个节点
    ===================================================
        // 条件队列
        Node nextWaiter; 
}
~~~

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201070939350.png" alt="image-20220107093859221" style="float:left" />

<font color=orange>阻塞队列不包含head节点</font>

### 10.2 ReentranLock

~~~java
// 我用个web开发中的service概念吧
public class OrderService {
    // 使用static，这样每个线程拿到的是同一把锁，当然，spring mvc中service默认就是单例，别纠结这个
    // 获得的是一个公平锁
    private static ReentrantLock reentrantLock = new ReentrantLock(true);

    public void createOrder() {
        // 比如我们同一时间，只允许一个线程创建订单
        reentrantLock.lock();
        // 通常，lock 之后紧跟着 try 语句
        try {
            // 这块代码同一时间只能有一个线程进来(获取到锁的线程)，
            // 其他的线程在lock()方法上阻塞，等待获取到锁，再进来
            // 执行代码...
            // 执行代码...
            // 执行代码...
        } finally {
            // 释放锁
            reentrantLock.unlock();
        }
    }
}
~~~

#### 10.2.1 争锁

~~~java
    // 争锁
	final void lock() {
        acquire(1);
    }
	    
	// 获取锁
	public final void acquire(int arg) {
        //1. 看看能否尝试获取锁成功
        //2. 获取失败将当前线程转为节点进入阻塞队列
        if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            // 因为线程被中断过，但中断状态为已经被清除了，调用下面这个方法重新设置中断状态
            selfInterrupt();
    }


	// 尝试获取锁
	protected final boolean tryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();
        // state=0代表锁没有被占用
        if (c == 0) {
            // 1.查看是否也有其他线程在阻塞队列等待获取锁
            // 2.原子方式设置状态位
            // 如果有一个返回false，说明有其他线程在阻塞或者抢先cas了
            if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current);
                return true;
            }
        }
        // state > 0，则代表可能是线程重入锁了，只要更新state就可以了
        else if (current == getExclusiveOwnerThread()) {
            int nextc = c + acquires;
            if (nextc < 0)
                throw new Error("Maximum lock count exceeded");
            setState(nextc);
            return true;
        }
        // 获取锁失败，则返回到上一级函数调用 if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        return false;
    }

	// 把当前线程转换为指定模式的节点并且入队
	private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        // 将节点接到阻塞队列中
        Node pred = tail;
        // 队列不为空 (当tail==null时说明没有阻塞队列,head=tail==null时则说明刚初始化)
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        // 队列为空或者CAS失败，其他线程抢先设置了尾节点入队
        enq(node);
        return node;
    }

	// 在队列为空或者CAS失败的情况下，以自旋的方式将线程节点接入到队列中
	private Node enq(final Node node) {
        // 自旋循环
        for (;;) {
            Node t = tail;
            // 队列为空
            if (t == null) { 
                // 设置头结点newNode() waitStatus = 0，后边可以设置
                if (compareAndSetHead(new Node()))
                    // 将tail指向head
                    // 注意，这里没有返回t,而是继续for循环,继续for循环,继续for循环,将node入队
                    tail = head;
            }
            // 和上一个方法一样，再次尝试接入到链表尾节点
            else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

	// 上面这个方法返回之后，又回到了if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
	// acquireQueued将会使线程阻塞和获取锁
	// 返回true：说明线程结束阻塞的方式是其他线程中断的
	// 返回false:说明线程结束阻塞的方式是LocksSupport.unpark()
	final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            // 不断的for循环，直到node获取了锁。(线程重新不阻塞后再次尝试获取锁)
            for (;;) {
                final Node p = node.predecessor();
                // node的前继节点为head，说明node是阻塞队列的第一个节点
                // 因此node节点可以去尝试获取锁，因为new Node()时没有设置任何线程,state为0
                if (p == head && tryAcquire(arg)) {
                    // 将node设置为头结点，node.thread=null, node.prev=null
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                // 走到这里则说明节点不再是阻塞队列的第一个节点了(本来就不是或者竞争锁失败)
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
	
	// 在争锁失败后挂起线程
	private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        // 之前设置了前一个节点的状态=-1，是正常的状态,当前线程可以安全挂起，等待以后前驱结点释放锁时被唤醒（前面的for循环中可能会不断调用这个方法）
        if (ws == Node.SIGNAL)
            return true;
        // waitstatus=1，则说明前继节点放弃了争锁(reentrantlock可以定时争锁)
        if (ws > 0) {
            // 寻找前继节点
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        }
        // waitStatus = 0（node初始化时），将它设置为-1
        else {
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
	// 如果上一个方法返回true 则说明线程可以安全挂起，而这个方法就是用来挂起线程的
	private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);  // 线程被阻塞在此
        // 线程结束阻塞时，会检查自己结束阻塞的原因是否因为中断
        // 如果是因为中断，返回true，之后清除中断的状态
        return Thread.interrupted();
    }
~~~

![image-20220107203022006](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201072030106.png)

#### 10.2.2 释放锁

~~~java
	public void unlock() {
        sync.release(1);
    }
	// 释放锁
	public final boolean release(int arg) {
        // 释放锁成功
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }
	// 尝试释放锁
	protected final boolean tryRelease(int releases) {
        int c = getState() - releases;
        if (Thread.currentThread() != getExclusiveOwnerThread())
            throw new IllegalMonitorStateException();
        boolean free = false;
        // 锁没有被占用
        if (c == 0) {
            free = true;
            setExclusiveOwnerThread(null);
        }
        setState(c);
        return free;
    }
	// 唤醒被挂起的线程
	private void unparkSuccessor(Node node) {
        // 清理node的状态
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);
		
        Node s = node.next;
        // 有可能后继节点取消了等待
        if (s == null || s.waitStatus > 0) {
            s = null;
            // 从队尾往前找，找到waitStatus<=0的所有节点中排在最前面的
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        // 唤醒线程
        if (s != null)
            LockSupport.unpark(s.thread);
    }
	// 唤醒之后的线程会回到这个方法中，然后继续循环
	private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);  // 线程被阻塞在此
        return Thread.interrupted();
    }
~~~

#### 10.2.3 总结

- AQS采用Locks.park(thread)和Locks.unpark(thread)来阻塞和解除阻塞
- 采用链表作为阻塞队列
- 通过state状态来查看锁是否被线程持有

#### 10.2.4 公平与非公平

公平锁与非公平锁在源码里只有两点区别:

- 非公平锁会直接先进行一次CAS(0,1),如果成功了就直接返回。
- 如果锁没有被占用(state=0)，非公平锁不会查看队列中是否还有其他线程，直接CAS尝试抢占

~~~java
static final class NonfairSync extends Sync {
        final void lock() {
            // 区别1：会尝试先进行一次CAS抢占锁
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }
    
    	final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                // 区别2：如果锁没被占用，直接CAS抢占锁，不会查看队列是否有其他线程
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }
~~~

### 10.3 条件队列

> 条件队列：使得一组线程能够通过某种方式来等待特定的条件变成真，常用于生产者消费者模式

#### 10.3.1 内置的条件队列

Java对象的内置锁和内置条件队列是相关联的，要想使用条件队列的API，就必须持有锁。换句话说,内置锁对象必须也是条件队列对象。

~~~java
// this对象既是锁对象又是条件队列对象
public synchronized put(V v){
    while(isFull()){
        this.wait();
    }
    doPut(v);
    this.notifyAll();
}
public synchronized take(){
    while(isEmpty()){
        this.wait();
    }
    doTake();
    this.notifyAll();
}
~~~

<font color=orange>多个线程基于不同的条件在同一个条件队列上等待。因此如果使用notify()而不是notifyAll()将是一个危险操作。</font>

- notifyAll():所有线程被唤醒后，会争抢锁。争到锁的线程会因为while循环再次判断条件是否满足，不满足则再次进入条件队列同时放弃锁，其他被唤醒的线程可以再次争抢锁。
- notify():选择一个线程被唤醒，但这个线程可能是由于不同的条件而等待，因此即便抢到锁也会因条件不满足再次进入条件队列。因此notify()信号丢失了。

可以发现，内置锁的缺陷是<font color=pink>只能拥有一个相关联的条件队列。</font>在notifyAll()会造成大量的上下文切换。

**ReentrantLock和Condition的结合解决了这个问题。**

#### 10.3.2 显示的条件队列

Lock与Condition一起使用，就如同内置锁和内置条件队列一样。但Lock可以拥有任意数量Condition（条件队列）

```java
class BoundedBuffer {
    final Lock lock = new ReentrantLock();
    // condition 依赖于 lock 来产生
    final Condition notFull = lock.newCondition();
    final Condition notEmpty = lock.newCondition();

    final Object[] items = new Object[100];
    int putptr, takeptr, count;

    // 生产
    public void put(Object x) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();  // 队列已满，等待，直到 not full 才能继续生产
            items[putptr] = x;
            if (++putptr == items.length) putptr = 0;
            ++count;
            notEmpty.signal(); // 生产成功，队列已经 not empty 了，发个通知出去
        } finally {
            lock.unlock();
        }
    }

    // 消费
    public Object take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await(); // 队列为空，等待，直到队列 not empty，才能继续消费
            Object x = items[takeptr];
            if (++takeptr == items.length) takeptr = 0;
            --count;
            notFull.signal(); // 被我消费掉一个，队列 not full 了，发个通知出去
            return x;
        } finally {
            lock.unlock();
        }
    }
}
```

<font color=pink>Condition对象使用await()和signal()</font>

#### 10.3.3 Condition对象

```java
public class ConditionObject implements Condition, java.io.Serializable {
    /** 条件队列的头结点 */
    private transient Node firstWaiter;
    /** 条件队列的尾节点 */
    private transient Node lastWaiter;
    ...
}
```

两个不同的概念：

- 条件队列：因为条件不满足而阻塞的线程，使用 nextWaiter 指针构成的单向链表
- 阻塞队列：因为争抢锁而阻塞的线程，使用 next、prev 指针构成的双向链表

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201081358869.png" alt="image-20220108135845703" style="zoom:80%;float:left" />

1. 线程1调用 `condition1.await()` 方法即可将当前线程 1 包装成 Node 后加入到条件队列中，然后**阻塞**在这里。
2. 线程2调用 `condition1.signal()` 方法，会将对应的**条件队列**的 firstWaiter（队头）移到**阻塞队列的队尾**，等待获取锁，获取锁后 await 方法才能返回，继续往下执行。

```java
## 调用await()方法，等待唤醒
public final void await() throws InterruptedException {
    // 查看线程是否被中断
    if (Thread.interrupted())
        throw new InterruptedException();
    // 1.将线程添加到条件队列中
    Node node = addConditionWaiter();
    // 2.释放锁，因为是可重入锁，返回重入的次数
    int savedState = fullyRelease(node);
    int interruptMode = 0;
    ==============================================================
        如果另一个线程执行condition.signal()，则节点将位于阻塞队列中
    ==============================================================
    // 3.节点是否已经从条件队列中移动到阻塞队列了呢？
    // 没有则阻塞当前线程
    while (!isOnSyncQueue(node)) {
        LockSupport.park(this); // 线程在此阻塞
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
    // 4.在阻塞队列中尝试获取锁
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
    if (node.nextWaiter != null) // clean up if cancelled
        unlinkCancelledWaiters();
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);
}
```

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201091216975.png" alt="image-20220109121608890" style="float:left" />

**1. 将节点加入条件队列**

```java
## 将当前线程添加进条件队列中
private Node addConditionWaiter() {
    Node t = lastWaiter;
    // 队列的最后一个节点已经取消等待某个条件，则会将它清除出去
    if (t != null && t.waitStatus != Node.CONDITION) {
        // 清楚所有取消等待的线程
        unlinkCancelledWaiters();
        t = lastWaiter;
    }
    // 将线程转换成节点添加进条件队列中
    Node node = new Node(Thread.currentThread(), Node.CONDITION);
    if (t == null)
        firstWaiter = node;
    else
        t.nextWaiter = node;
    lastWaiter = node;
    return node;
}
// 移除取消条件等待的节点
private void unlinkCancelledWaiters() {
    Node t = firstWaiter;
    Node trail = null; // 跟踪指针
    /****
    tail -> t -> next
    *****/
    while (t != null) {
        Node next = t.nextWaiter;
        // t已经取消条件队列上的等待, 移除t
        if (t.waitStatus != Node.CONDITION) {
            t.nextWaiter = null;
            if (trail == null)
                firstWaiter = next;
            else
                trail.nextWaiter = next;
            if (next == null)
                lastWaiter = trail;
        }
        // t仍然是条件队列的状态，更新tail
        else
            trail = t;
        t = next;
    }
}
```

**2. 执行`fullyRealse()`，当前线程释放锁**

~~~java
final int fullyRelease(Node node) {
    boolean failed = true;
    try {
        // 获取线程重入锁的次数
        int savedState = getState();
        // 完全释放锁
        // 成功就返回
        // 失败就抛出异常，同时
        if (release(savedState)) {
            failed = false;
            return savedState;
        } else {
            throw new IllegalMonitorStateException();
        }
    } finally {
        // 将线程状态设为Cancelled，这个节点会被清除(在之前的代码解析中说过‘shouldParkAfterFailure’)
        if (failed)
            node.waitStatus = Node.CANCELLED;
    }
}

public final boolean release(int arg) {
    if (tryRelease(arg)) {
        // 唤醒下一个节点线程
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
// 返回true则说明释放完毕，线程不再占有锁
// 返回false则说明异常
protected final boolean tryRelease(int releases) {
    int c = getState() - releases;
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    // c == 0,说明全部释放完毕，返回true,同时更新没有线程持有锁
    if (c == 0) {
        free = true;
        setExclusiveOwnerThread(null);
    }
    // c != 0 返回false，这是错误的
    setState(c);
    return free;
}
~~~

**3. 等待进入阻塞队列**

~~~java
// 返回true则说明节点已经位于阻塞队列中
// 返回false则说明还在条件队列
final boolean isOnSyncQueue(Node node) {
    // 移动过去的时候，node 的 waitStatus 会置为 0，这个在说 signal 方法的时候会说到
    // 如果 waitStatus 还是 Node.CONDITION，也就是 -2，那肯定就是还在条件队列中
    // 如果 node 的前驱 prev 指向还是 null，说明肯定没有在 阻塞队列(prev是阻塞队列链表中使用的)
    if (node.waitStatus == Node.CONDITION || node.prev == null)
        return false;
    // 如果next指针不为空，说明已经在阻塞队列
    if (node.next != null) 
        return true;
    // 可以通过判断 node.prev() != null 来推断出 node 在阻塞队列吗？答案是：不能。
    // 这个可以看 AQS 的入队方法，首先设置的是 node.prev 指向 tail，
    // 然后是 CAS 操作将自己设置为新的 tail，可是这次的 CAS 是可能失败的。
    return findNodeFromTail(node);
}
// 从后往前查找是否有node在阻塞队列中
private boolean findNodeFromTail(Node node) {
    Node t = tail;
    for (;;) {
        if (t == node)
            return true;
        if (t == null)
            return false;
        t = t.prev;
    }
}
~~~

**4. signal()将线程移动到阻塞队列**

```java
// 1. 让阻塞的线程节点移动到阻塞队列中
// 2. 让阻塞的线程继续执行 
public final void signal() {
    // 检查调用这个方法的线程是否是独占了锁
    if (!isHeldExclusively())
        throw new IllegalMonitorStateException();
    // 将条件队列中等待最久的线程移入阻塞队列
    Node first = firstWaiter;
    if (first != null)
        doSignal(first);
}
private void doSignal(Node first) {
    do {
        // firstWaiter更新为条件队列中的下一个节点
        if ( (firstWaiter = first.nextWaiter) == null)
            lastWaiter = null;
        //因为节点即将移入到阻塞队列，因此切断节点和条件队列的关系
        first.nextWaiter = null;
    } while (!transferForSignal(first) &&
             (first = firstWaiter) != null);
}
// 移动节点到阻塞队列
final boolean transferForSignal(Node node) {
    // 如果无法设置waitStatus，说明节点已经设置为cancelled状态（在fullyRealse方法中）,则不用进入阻塞队列了
    if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
        return false;
    // 执行enq方法将节点接入，返回前一个节点
    Node p = enq(node);
    int ws = p.waitStatus;
    // 设置前一个节点的状态为-1(前面讲过，前一个节点的状态由后一个节点更新)
    // 如果状态本来就是已关闭，或者CAS设置失败，重新同步
    if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
        LockSupport.unpark(node.thread);
    return true;
}
```

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201091219799.png" alt="image-20220109121934719" style="float:left" />

**5. 唤醒后检查中断的状态**

线程能够结束LockSupport.park()阻塞的的原因:

- 进入阻塞队列后成功被唤醒
- signal()方法中因为CAS失败或者前一个节点线程已经取消
- 线程中断
  - 在signal()方法调用前,说明节点还在条件队列时被中断（需要将节点转移到阻塞队列，然后抛出异常）
  - 在signal()方法调用后,说明节点已经在阻塞队列或者即将进入阻塞队列（所以会有Thread.yeild()让其完成转移) 

interruptMode:

- REINTERRUPT： 代表 await 返回的时候，需要重新设置中断状态,在signal()方法调用之后中断
- THROW_IE： 代表 await 返回的时候，需要抛出 InterruptedException 异常,signal()方法调用前中断
- 0 ：说明在 await 期间，没有发生中断

~~~java
// 退出循环的条件时：1.成功入队 2.中断
while (!isOnSyncQueue(node)) {
        LockSupport.park(this); // 后面的signal()方法会唤醒
    	===============
            结束阻塞
        ===============
    	//线程继续执行后首先判断自己是否是因为中断才唤醒的
        //如果是因为中断，则判断是signal()前还是signal()后
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
}
// 检查是否是阻塞期间发生了中断?
private int checkInterruptWhileWaiting(Node node) {
    return Thread.interrupted() ?
        (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) : 0;
}
// 判断中断是什么时候发生的
final boolean transferAfterCancelledWait(Node node) {
    // 如果waitStatus为condition，则说明是在signal()方法前发生的中断
    if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
        // 即便是在条件队列中被中断，也移入阻塞队列，但因此会抛出异常
        enq(node);
        return true;
    }
    // 到这里肯定是因为signal()方法已经开始执行,因为signal会将节点状态设为0，转移到阻塞队列
    // while循环的作用是确保节点成功入阻塞队列
    // 当然，这种事情还是比较少的吧：signal 调用之后，没完成转移之前，发生了中断
    while (!isOnSyncQueue(node))
        Thread.yield();
    return false;
}
~~~

**6. 获取独占锁**

线程进入阻塞队列之后又可以重新尝试获取锁了

~~~java
//acquireQueued(node, savedState) 的返回值就是代表线程是否被中断
//如果返回 true，说明被中断了，而且 interruptMode != THROW_IE，说明在 signal 之后发生中断了
//这里将 interruptMode 设置为 REINTERRUPT，用于待会重新中断。
if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
// 如果在signal()前发生中断，那么即便中断也会入队(transferAfterCancelledWait)
// 没有设置node.nextWaiter==null
// 下述的代码就是将因为中断而进入阻塞队列的节点清除其在条件队列上的位置
if (node.nextWaiter != null) 
        unlinkCancelledWaiters();

if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);
~~~

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201091217781.png" alt="image-20220109121724682" style="zoom:80%;float:left" />

**7. 处理中断状态**

到这里，我们终于可以好好说下这个 interruptMode 干嘛用了。

- 0：什么都不做，没有被中断过；
- THROW_IE：让await方法抛出 InterruptedException 异常，因为它代表在 await() 期间发生了中断；
- REINTERRUPT：重新中断当前线程，因为它代表 await() 期间没有被中断，而是 signal() 以后发生的中断

```java
private void reportInterruptAfterWait(int interruptMode)
    throws InterruptedException {
    if (interruptMode == THROW_IE)
        throw new InterruptedException();
    else if (interruptMode == REINTERRUPT)
        // 重新记录中断的状态
        selfInterrupt();
}
```

## 第十一章 中断

### 10.1 API的说明

> 在Java中，中断其实就是设置一个boolean类型的中断状态
>
> 中断一个线程，目标线程的中断状态将被设置为true

~~~JAVA
// 中断目标线程，目标线程的中断状态将被设置为true
void interrupt() {
    if (this != Thread.currentThread())
            checkAccess();

    synchronized (blockerLock) {
        Interruptible b = blocker;
        if (b != null) {
            // 设置中断状态位
            interrupt0();         
            b.interrupt(this);
            return;
        }
    }
    interrupt0();
}
~~~

1. 如果线程A阻塞于`obj.wait()`...或者`Thread.sleep()`、`Thread.join()`...当线程B调用`A.interrupt()`会**先设置A的中断状态为true。**

​    **wait()等阻塞库方法会检查A的中断状态，一旦发现中断会提前返回，然后自动清除A的中断状态**、抛出InterruptedException。

2. 如果线程A阻塞于`InterruptibleChannel`，但是中断状态会保持，目标线程会抛出ClosedByInterruptException。
3. 如果线程A阻塞于`Selector`，那么中断状态也不会被清除，同时会立即从select操作中立即返回。
4. 如果线程A阻塞于`LockSupport.park`,那么中断状态不会被清除，但方法检测到中断会唤醒线程
5. 上述3中情况都不是，那么仅仅改变目标线程的中断状态

~~~java
public boolean isInterrupted() {
    return isInterrupted(false);
}
~~~

查看目标线程是否被中断了。调用这个方法不会改变中断状态。

- true:目标线程被中断了
- false:其他情况

```java
// 静态方法
public static boolean interrupted() {
    return currentThread().isInterrupted(true);
}
```

查看当前线程是否被中断了，<font color=orange>清除线程的中断状态。</font>

换句话说，如果连续两次调用该方法，第二次调用会返回false。

- true:当前线程被中断了
- false:其他情况

### 10.2 响应中断

> 当看到方法上带有 `throws InterruptedException` 时，就要知道这个方法应该是阻塞方法，我们如果希望它能早点返回的话，我们往往可以通过中断来实现。 

如果线程在阻塞期间被中断，那么他可以选择不处理也可以选择处理

- 不处理，记录中断的状态位(LockSupport.park())或返回代表中断的boolean类型结果
- 处理，结束阻塞抛出InterruptedException(Thread.sleep()、obj.wait())

~~~java
// 如果方法抛出InterruptedException，那么方法的开头会进行中断的判断
public final void await() throws InterruptedException {
    if (Thread.interrupted()) {
        throw new InterruptedException();
	}
    ...
}

public final void acquireInterruptibly(int arg) throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    if (!tryAcquire(arg))
        doAcquireInterruptibly(arg);
}
// 方法不抛出异常，但记录中断的状态位，selfInterrupt会重新中断
public final void acquire(int arg) {
    if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
static void selfInterrupt() {
    Thread.currentThread().interrupt();
}
// queue.take()方法在取得任务前即便中断也会继续循环进行，在取得任务后中断
try{
    while(true){
        try{
            queue.take();
        }catch(InteruptedException e){
            interrupted = true;
            Thread.interrupted();
        }
    }
}
finally{
    if(interrupted){
        Thread.currentThread.interrupt();
    }
}

~~~

## 第十二章 Java内存模型

> 为了提高程序的执行效率，编译器和处理器会对指令进行重排序，但在并发操作下会导致数据安全问题。如果操作能符合happens-before规则，则程序员不用担心重排序的问题。

### 12.1 happens-before规则

Java内存模型为所有的操作定义了一个偏序关系，称为<font color=orange>"happens-before"</font>，提供了可见性与顺序性保证。

要想保证执行操作B的线程可以见到操作A的结果，那么操作A必须<font color=orange>happens-before</font>操作B.

Java中符合happens-before规则的包括：

- 在一个单独的线程中，按照程序代码的执行流顺序，（时间上）先执行的操作happen—before（时间上）后执行的操作
- 对一个锁的解锁在这个锁的加锁前执行。
- 对一个volatile域的写在任意后续对这个volatile域的读之前执行
- 如果A happens-before B，且B happens-before C，那么A happens-before C。
- 对线程thread.start的调用必须在该线程中执行任何操作之前执行

**JMM会寻求一个happens-before的平衡点：既向程序员保证程序的顺序性，又要让底层的编译器能对指令重排序提高效率。**

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201141310955.webp" alt="img" style="zoom:80%;float:left" />

**只要不改变程序的执行结果（单线程程序和正确同步了的多线程程序），编译器和处理器怎么优化都行。**

在AQS同步类构造的Latch、Semaphore中，利用了`volatile`的可见性与happens-before规则的排序功能。

### 12.2 volatile的原理

对于volatile修饰的变量，编译器会在生成字节码序列时，在指令序列中添加<font color=orange>内存栅栏</font>

- 对volatile变量的写操作之后加入写屏障
- 对volatile变量的读操作之前加入写屏障

写屏障保证在该屏障之前，对共享变量的改动都同步到主内存中（可见性），同时保证不进行重排序（有序性）。

读屏障保证在该屏障之后，对共享变量的读取加载的是主内存中最新的值，同时保证不进行重排序。

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201132158509.png" alt="image-20220113215830407" style="zoom:80%;float:left" />

缺点：volatile无法保证原子性，所以在`checkAndSet`中还是存在线程安全问题。

### 12.3 发布

不正确发布的真正原因：是在"发布一个对象"与"另一个线程访问对象"之间缺乏一种happens-before排序

换句话说，在对象还没发布完对象前，另一个线程已经开始访问对象了

#### 12.3.1 不安全的发布

发布一个对象的步骤：

1. 初始化一个对象时写入多个变量的初始值(域)
2. 将变量写入引用

这两个步骤会发生重排序，如果无法确保发布一个对象的操作happens-before于另一个线程读取该对象的操作，那么无法保证发布对象的可见性。

在这种情况下，另一个线程可能读取到被部分构造的对象。

~~~java
public class UnsafeLazyInitialization {
    private static Resource resource;
    public static Resource getInstatnce(){
        // 不考虑竞态条件问题，这个类也是线程不安全的
        // 另一个线程可能看到部分构造的实例引用
        if(resource == null){
            resource = new Resource();
        }
        return resource;
    }
}
~~~

#### 12.3.2 安全初始化方式

> 可以使用下面几种方法来确保两个操作的happens-before(可见性) `与第三章的安全发布内容一致`

1. 声明为synchronized方法

~~~java
public class SafeLazyInitialization{
    private static Resource resource;
    public synchronized static Resource getInstance(){
        if(resource == null){
			resource = new Resource();
        }
        return resource;
    }
}
~~~

2. 提前初始化

~~~java
public class EagerInitialization {
    private static Resource resource = new Resource();
    public static Resource getResource(){
        return resource;
    }
}
~~~

3. 静态内部类

~~~java
public class ResourceFactory{
    public static class ResourceHolder{
        public static Resource resource = new Resource();
    }
    public static Resource getResource(){
        return ResourceHolder.resource;
    }
}
~~~

4. 双重检查加锁

~~~java
public class DoubleCheckLocking{
    private static volatile Resource resource;
    public static Resource getInstance(){
        if(resouce == null){
            synchronized(DoubleCheckLocking.class){
                if(resouce == null){
                    resource = new Resource();
                }
            }
        }
        return resource;
    }
}
~~~

5. 不可变对象

~~~java
public class SafeLazyInitialization{
    private static final Resource resource;
    public synchronized static Resource getInstance(){
        if(resource == null){
			resource = new Resource();
        }
        return resource;
    }
}
~~~

