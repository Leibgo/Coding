# CountDownLatch

## 1. 使用场景

>等待所有线程都执行完相同的事情后，才允许放行

需要注意的点是：CountDownLatch本身是基于**<font color=red>共享锁</font>**实现的

JDK源码中提供了关于CountDownLatch的使用方式：

1. 使用两个锁控制一段流程

   ~~~java
   class Driver { 
    *   void main() throws InterruptedException {
    *     CountDownLatch startSignal = new CountDownLatch(1);
    *     CountDownLatch doneSignal = new CountDownLatch(N);
    *
    *     for (int i = 0; i < N; ++i) // create and start threads
    *       new Thread(new Worker(startSignal, doneSignal)).start();
    *
    *     doSomethingElse();            // don't let run yet
    *     startSignal.countDown();      // let all threads proceed
    *     doSomethingElse();
    *     doneSignal.await();           // wait for all to finish
    *   }
    * }
    *
    * class Worker implements Runnable {
    *   private final CountDownLatch startSignal;
    *   private final CountDownLatch doneSignal;
    *   Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
    *     this.startSignal = startSignal;
    *     this.doneSignal = doneSignal;
    *   }
    *   public void run() {
    *     try {
    *   	 // 所有线程阻塞在此
    *       startSignal.await();
    *       doWork();
    *       doneSignal.countDown();
    *     } catch (InterruptedException ex) {} // return;
    *   }
    *
    *   void doWork() { ... }
    * }
   ~~~

## 2. 源码

```java
public CountDownLatch(int count) {
    if (count < 0) throw new IllegalArgumentException("count < 0");
    this.sync = new Sync(count);
}
```

通过构造函数传入的参数count，直接设置了state

因此state在countdownlatch里的含义：**要求执行完某个任务的线程数，只有达到这个数量才允许放行**

### 2.1 Sync

与ReetrantLock一样，CountDownLatch有内部类`Sync`实现具体的模板方法：获取共享锁，释放共享锁

~~~java
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
        // Decrement count; signal when transition to zero
        for (;;) {
            int c = getState();
            if (c == 0)
                return false;
            int nextc = c - 1;
            if (compareAndSetState(c, nextc))
                return nextc == 0;
        }
    }
}
~~~

`tryAcquireShared`方法用来尝试获取共享锁，如果有state个线程执行过任务了返回1，否则返回-1代表获取共享锁失败

`tryReleaseShared`方法用来尝试释放共享锁，将state减1，CAS设置state失败则循环，成功则判断state是否等于0

**换言之：只有N个以后的线程才能成功获取共享锁**

### 2.2 await

<img src="E:\loubei\学习资料\Coding\笔记\并发\CountDownLatch.assets\image-20220429182155550.png" alt="image-20220429182155550" style="zoom:120%;float:left" />

~~~java
public void await() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
}
~~~

`await`方法便是CountDownLatch向外提供的获取共享锁的接口，方法体里直接调用AQS的模板方法`acquireSharedInterruptibly`，可中断的获取共享锁

~~~java
public final void acquireSharedInterruptibly(int arg) throws InterruptedException {
    // 检查线程是否中断
    if (Thread.interrupted()){
        throw new InterruptedException();
    }
    if (tryAcquireShared(arg) < 0){
        doAcquireSharedInterruptibly(arg);
    }
}
~~~

调用了`tryAcquireShared`，检查它的返回：

~~~java
protected int tryAcquireShared(int acquires) {
    return (getState() == 0) ? 1 : -1;
}
~~~

虽然是获取共享锁，但实际上只是检查state是否为0：

- 如果还不足N个线程执行过某个任务，则`tryAcquireShared`调用`doAcquireSharedInterruptibly`，获取共享锁失败，返回-1

- 所以如果有N个线程执行过任务了，返回1。

为什么说CountDownLatch不能重置屏障？原因就在这：

~~~java
if (tryAcquireShared(arg) < 0){
    doAcquireSharedInterruptibly(arg);
}
~~~

CountDownLatch只对前N个线程有效。**N个以后的线程都是成功获取了共享锁然后直接返回，不会重置Latch**

~~~java
private void doAcquireSharedInterruptibly(int arg) throws InterruptedException {
    final Node node = addWaiter(Node.SHARED);
    try {
        for (;;) {
            final Node p = node.predecessor();
            // 前继节点是head，会再有一次机会获取共享锁
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    return;
                }
            }
            // 获取失败则阻塞
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                throw new InterruptedException();
        }
    } catch (Throwable t) {
        cancelAcquire(node);
        throw t;
    }
}
~~~

阻塞队列会查看当前节点的前任节点，如果是head节点会再给一次机会尝试获取共享锁，成功则返回，失败则阻塞

### 2.3 countDown

流程图:

<img src="E:\loubei\学习资料\Coding\笔记\并发\CountDownLatch.assets\image-20220429180640886.png" alt="image-20220429180640886" style="zoom:120%;float:left" />

~~~java
public void countDown() {
    sync.releaseShared(1);
}
~~~

countDown方法还是调用了AQS的`releaseShared`

~~~java
public final boolean releaseShared(int arg) {
    // 如果是第N个线程执行任务，则唤醒后继节点
    if (tryReleaseShared(arg)) {
        doReleaseShared();
        return true;
    }
    return false;
}
~~~

检查`tryReleaseShared`的返回值：

~~~java
protected boolean tryReleaseShared(int releases) {
    // Decrement count; signal when transition to zero
    for (;;) {
        int c = getState();
        // N个以后的线程, state==0 直接返回
        if (c == 0){
           return false; 
        }
  		// 更新state 
        int nextc = c - 1;
        // CAS成功，并且恰好是第N个线程时唤醒持有共享锁的阻塞节点
        if (compareAndSetState(c, nextc))
            return nextc == 0;
    }
}
~~~

有且仅有恰好是第N个线程执行时，返回True。其余线程都返回False

若返回true，则调用`doReleaseShared`方法，唤醒其余阻塞的线程

~~~java
private void doReleaseShared() {
    for (;;) {
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) {
                // CAS设置node的状态
                if (!h.compareAndSetWaitStatus(Node.SIGNAL, 0)){
                    continue;          
                }
                // 唤醒后继者
                unparkSuccessor(h);
            }
            else if (ws == 0 && !h.compareAndSetWaitStatus(0, Node.PROPAGATE)){
                continue;                
            }
        }
        if (h == head)                   
            break;
    }
}
~~~

## 3. 总结

1. CountDownLatch是基于**共享锁**实现
2. 初始化时指定线程的个数N，并赋值给State
3. 调用`await`方法时，判断state是否为0，前N个线程会阻塞在等待队列中
4. 调用`countDown`方法时，前N个线程修改State，第N个线程修改时State=0，负责唤醒阻塞在等待队列中的线程
5. N之后的线程调用`countDown`时，会因为state = 0而成功返回