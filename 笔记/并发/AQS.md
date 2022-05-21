# AQS

## 基本架构

## Reetrantlock

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

