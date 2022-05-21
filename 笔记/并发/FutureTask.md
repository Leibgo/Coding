# FutureTask

### 13.1 Future和Runnable

- FutureTask实现了`RunnableFuture`接口 

- `RunnableFuture`接口继承了Runnable接口和Future接口

~~~java
// Runnable：代表一个任务，没有执行结果
public interface Runnable {
    public abstract void run();
}
~~~

~~~java
// Future：代表一个任务的生命周期，可以获取任务的结果，中断任务，以及查看任务的状态
public interface Future<V> {
    
    boolean cancel(boolean mayInterruptIfRunning);

    boolean isCancelled();

    boolean isDone();

    V get() throws InterruptedException, ExecutionException;

    V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
}
~~~

### 13.2 FutureTask

#### 13.2.1 成员变量

~~~java
public class FutureTask<V> implements RunnableFuture<V> {
    // 任务的状态
    private volatile int state;
   
    // 执行的任务
    private Callable<V> callable;
   	
    // 执行的结果
    private Object outcome;
    
    // 执行任务的线程
    private volatile Thread runner;
   	
    // 单向链表队列：因任务还未完成的阻塞的线程
    private volatile WaitNode waiters;
}
~~~

#### 13.2.2 构造函数

~~~java
// 构造函数
// 1. 将runnable转为callable
public FutureTask(Runnable runnable, V result) {
    this.callable = Executors.callable(runnable, result);
    this.state = NEW;      
}
// 2. 直接传入callable
public FutureTask(Callable<V> callable) {
    if (callable == null) throw new NullPointerException();
    this.callable = callable;
    this.state = NEW;       
}
~~~

### 13.3 JUC的三大法宝

> 在AQS中，获取独占锁失败的线程便会阻塞，它是通过以下三点来实现的：
>
> - 状态：用状态state来决定是否获取锁失败
> - 队列：将获取锁失败的线程转为节点插入队列
> - CAS：由于是并发环境，插入队列、获取锁的操作都需要确保是原子性的
>
> 在FutureTask中，也是通过上面的三大法宝来实现自己的接口

#### 13.3.1 状态

~~~java
// 任务的状态
private volatile int state;

// 任务在运行中
private static final int NEW          = 0;

// 任务运行完成，正在设置运行结果
private static final int COMPLETING   = 1;

// 任务执行完毕
private static final int NORMAL       = 2;

// 任务运行过程中抛出异常
private static final int EXCEPTIONAL  = 3;

// 任务被取消
private static final int CANCELLED    = 4;

// 任务正在被中断
private static final int INTERRUPTING = 5;

// 任务被中断
private static final int INTERRUPTED  = 6;
~~~

<img src="E:\loubei\学习资料\Coding\笔记\并发\FutureTask.assets\image-20220428133532076.png" alt="image-20220428133532076" style="zoom:80%;float:left" />

#### 13.3.2 阻塞队列

线程如果在任务还未完成时去获取任务的结果，则该线程会被阻塞，转换成`WaitNode`节点以头插法的方式插入waiters，可以理解为栈。

~~~java
static final class WaitNode {
    volatile Thread thread;
    volatile WaitNode next;
    WaitNode() { thread = Thread.currentThread(); }
}
~~~

<img src="E:\loubei\学习资料\Coding\笔记\并发\FutureTask.assets\image-20220428133457153.png" alt="image-20220428133457153" style="zoom:80%;float:left" />

#### 13.3.3 CAS

~~~java
// 通过反射获取成员变量的内存地址offset，之后进行unsafe的cas操作
private static final sun.misc.Unsafe UNSAFE;
private static final long stateOffset;
private static final long runnerOffset;
private static final long waitersOffset;
static {
    try {
        UNSAFE = sun.misc.Unsafe.getUnsafe();
        Class<?> k = FutureTask.class;
        stateOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("state"));
        runnerOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("runner"));
        waitersOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("waiters"));
    } catch (Exception e) {
        throw new Error(e);
    }
}
~~~

### 13.4 接口实现

#### 13.4.1 run()

> 当前线程执行callable任务

~~~java
public void run() {
    // 任务已经被其他线程执行了，或者没有成功CAS将当前线程设置FutureTask的执行线程，直接返回
    if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread())){
        return;
    }
    try {
        Callable<V> c = callable;
        if (c != null && state == NEW) {
            V result;
            boolean ran;
            try {
                // 执行任务，完毕后返回
                result = c.call();
                ran = true;
            } catch (Throwable ex) {
                result = null;
                ran = false;
                // 发生异常，将异常对象设置为输出
                setException(ex);
            }
            // 没抛出异常，将运行的结果设置为输出
            if (ran)
                set(result);
        }
    } finally {
        // 运行线程也设置为null
        runner = null;
        int s = state;
        // 检查是否线程被中断
        if (s >= INTERRUPTING)
            handlePossibleCancellationInterrupt(s);
    }
}

1> 任务执行成功后，进行输出设置后运行结果，并唤醒阻塞的线程
protected void set(V v) {
    // 将状态CAS设置为COMPLETING
    if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
        // 设置输出
        outcome = v;
        // 更新状态为NORMAL
        UNSAFE.putOrderedInt(this, stateOffset, NORMAL);
        // 唤醒阻塞的线程
        finishCompletion();
    }
}

2> 任务抛出异常后，将输出设置为异常对象，并唤醒阻塞的线程
protected void setException(Throwable t) {
    if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
        // 设置结果为Throwable异常对象
        outcome = t;
        // 更新状态为EXCEPTIONAL
        UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); 
        // 唤醒阻塞的线程
        finishCompletion();
    }
}


private void finishCompletion() {
    // 唤醒因为读取任务而阻塞在队列中的线程
    for (WaitNode q; (q = waiters) != null;) {
        // 将waiter设置为null
        if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
            // 将阻塞线程唤醒
            for (;;) {
                Thread t = q.thread;
                if (t != null) {
                    q.thread = null;
                    LockSupport.unpark(t);
                }
                WaitNode next = q.next;
                if (next == null)
                    break;
                q.next = null;
                q = next;
            }
            break;
        }
    }

    done();
	// 任务结束，设置为null
    callable = null;
}

private void handlePossibleCancellationInterrupt(int s) {
	// 自旋等待，直到任务被成功中断
    if (s == INTERRUPTING)
        while (state == INTERRUPTING)
            Thread.yield(); 
}
~~~

#### 13.4.2 get()

FutureTask设计两类线程：

1. 执行任务的线程，它只有一个，run方法由它执行
2. 获取执行结果的线程，可以有多个，通过get方法获取任务执行的结果，如果没有执行完毕，在阻塞队列阻塞，直到任务结束唤醒

~~~java
public V get() throws InterruptedException, ExecutionException {
    int s = state;
    // 如果任务正在执行或者正在设置结果，调用awaitDone
    if (s <= COMPLETING)
        // awaitdown返回任务的状态
        s = awaitDone(false, 0L);
    return report(s);
}
~~~

~~~java
// 等待完成
private int awaitDone(boolean timed, long nanos) throws InterruptedException {
    final long deadline = timed ? System.nanoTime() + nanos : 0L;
    // 准备给当前线程的节点对象
    WaitNode q = null;
    // 是否已入队
    boolean queued = false;
    for (;;) {
        // 线程的阻塞被中断
        if (Thread.interrupted()) {
            // 移除节点，抛出异常
            removeWaiter(q);
            throw new InterruptedException();
        }
		
        int s = state;
        // 1. 任务已经结束
        if (s > COMPLETING) {
            // 如果节点对象不为null，说明已经转为节点，对q进行处理
            if (q != null)
                q.thread = null;
            // 否则说明还没有转为节点时就已经有结果了，返回
            return s;
        }
        // 2. 如果任务处于中间态，意味着马上有结果了，线程切换
        else if (s == COMPLETING)
            Thread.yield();
        // 3. 任务正在运行中
        else if (q == null)
            q = new WaitNode();
        // 还没有阻塞入队，头插法插入链表
        else if (!queued)
            queued = UNSAFE.compareAndSwapObject(this, waitersOffset,q.next = waiters, q);
        ...
        // 阻塞当前线程
        else
            LockSupport.park(this);
    }
}

// 报告结果
private V report(int s) throws ExecutionException {
    Object x = outcome;
    // 输出结果
    if (s == NORMAL)
        return (V)x;
    // 任务被关闭的异常
    if (s >= CANCELLED){
    	throw new CancellationException();
    }
    // 任务执行出错的异常
    throw new ExecutionException((Throwable)x);
}
~~~

~~~java
// 移除队列中阻塞被中断的节点
private void removeWaiter(WaitNode node) {
    if (node != null) {
        // 将节点的线程设为null
        node.thread = null;
        retry:
        // 查找线程为null的节点，移除
        for (;;) {          
            for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                s = q.next;
                if (q.thread != null)
                    pred = q;
                else if (pred != null) {
                    // 移除q节点
                    // pred -> q -> s ====  pred -> s
                    pred.next = s;
                    if (pred.thread == null)
                        continue retry;
                }
                else if (!UNSAFE.compareAndSwapObject(this, waitersOffset, q, s))
                    continue retry;
            }
            break;
        }
    }
}
~~~

