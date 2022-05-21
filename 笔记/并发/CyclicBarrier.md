# CyclicBarrier

## 1. 特点

> 等待所有线程都到达后，再开始放行

CyclicBarrier的特点：

- CyclicBarrier基于`条件队列`和`独占锁`来实现，而非共享锁。
- CyclicBarrier可**重复使用**，在所有线程都到齐了一起通过后，将会开启新的一代。
- CyclicBarrier使用了`“all-or-none breakage model”`，所有互相等待的线程，**要么一起通过barrier，要么一个都不要通过**，如果有一个线程因为中断，失败或者超时而过早的离开了barrier，则该barrier会被broken掉，所有等待在该barrier上的线程都会抛出`BrokenBarrierException`（或者`InterruptedException`）。

通俗点说：

旅游团组织老年人旅游，每次不多不少只招100人，未满100人就放弃旅游，超过100人就组织下一个旅游团，满100人就旅游

如果旅游团在组织的过程中有人因为身体原因退出这个团，则这个团也就解散了

## 2. 源码解析

~~~java
public class CyclicBarrier {
    
    // 可重入锁
    private final ReentrantLock lock = new ReentrantLock();
    
    // 用于阻塞线程的Condtion
    private final Condition trip = lock.newCondition();
    
    // 用户期望的数量
    private final int parties;
    
    // 最后一个线程到达，冲破屏障时做的动作，可用户指定
    private final Runnable barrierCommand;
    
    // 当前Generation
    private Generation generation = new Generation();
    
    // 线程的数量：用于指定当前还需要几个线程才能冲破屏障
    private int count;
~~~

用Generation表示一个屏障

~~~java
private static class Generation {
    Generation() {}                 
    // 用broken表示线程是否出现中断或其他异常
    boolean broken;
}
~~~

CyclicBarrier使用ReentrantLock实现功能需求，同时结合了ReentrantLock和Condition

**`await`方法的流程图**：

<img src="E:\loubei\学习资料\Coding\笔记\并发\CyclicBarrier.assets\image-20220428134320816.png" alt="image-20220428134320816" style="zoom:95%;float:left" />

`await`方法其实就是调用了`doWait`，真正的核心业务在该方法里

~~~java
public int await() throws InterruptedException, BrokenBarrierException {
    try {
        return dowait(false, 0L);
    } catch (TimeoutException toe) {
        throw new Error(toe); 
    }
}
~~~

`doWait`方法主要分为两个步骤：

1. 线程抵达屏障时count数减一，如果count == 0则最后一个到达的线程执行冲破屏障的行为
2. 如果count  != 0，则阻塞该线程

~~~java
private int dowait(boolean timed, long nanos) throws InterruptedException, BrokenBarrierException {
    final ReentrantLock lock = this.lock;
    // 上锁
    lock.lock();
        
    try {
        // 0. 异常情况
        {
            ...
        }
        // 1. 所有线程都达到冲破屏障的代码逻辑
        {
            ...
        }
        // 2. 阻塞线程的代码逻辑(第二部分)
        {
            ...
        }
    } 
    
    // 解锁
    finally {
        lock.unlock();
    }
}
~~~

首先看第一部分的源码

~~~java
try{
    // 每到达一个线程,count--
    int index = --count;
    // index = 0 时，说明已达到阻塞的线程数,可以冲破屏障了
    if (index == 0) {  
        boolean ranAction = false;
        try {
            // 执行指定的【冲破屏障后的行为】
            final Runnable command = barrierCommand;
            if (command != null)
                command.run();
            ranAction = true;

            // 调用重置屏障
            nextGeneration();
     
            return 0;
         } finally {
            // 行为出错
            if (!ranAction)
                breakBarrier();
        }
    }
}                 
~~~

`nextGeneration`有两个作用：

- 唤醒等待队列中的线程
- 重置屏障

~~~java
private void nextGeneration() {
    // 唤醒等待队列中的线程，将线程转入到阻塞队列
    trip.signalAll();
    // 重置generation
    count = parties;
    generation = new Generation();
}
~~~

冲破屏障的行为出错，就执行`breakBarrier`，唤醒剩余线程，设置Generation的状态位broken = true，其他线程执行该方法时直接抛出异常

~~~java
private void breakBarrier() {
    generation.broken = true;
    count = parties;
    trip.signalAll();
}
~~~

现在查看第二部分的源码，当前线程不是第n个线程，则进行阻塞

~~~java
// 达到这一步，说明剩余的线程数量还未到达0
for (;;) {
    try {
        // 进入条件队列阻塞线程
        if (!timed)
            trip.await();
        else if (nanos > 0L)
            nanos = trip.awaitNanos(nanos);
    }
    // 有线程在阻塞过程中被中断，则条件队列里的所有线程都结束阻塞，抛出 brokenBarrier异常
    catch (InterruptedException ie) {
        if (g == generation && ! g.broken) {
            breakBarrier();
            throw ie;
        } else {
            Thread.currentThread().interrupt();
        }
    }

    if (g.broken)
        throw new BrokenBarrierException();
    // 说明已经重置了，一切正常，返回
    if (g != generation)
        return index;
} 
~~~

## 3. 总结

1. 每个线程执行await()方法时都会判断count的值，来决定线程的下一步行为
2. 每次到达指定的线程数时，会重置屏障，给下一批线程使用。这里的屏障通过一个简单的内部类Generation来表示

## 4. 使用场景

> 使用CyclicBarrier模拟多线程并发

~~~java
/**
 * 模拟多线程并发测试单例模式
 */
public class SingetonTest {
    class Worker implements Runnable{
        CyclicBarrier cyclicBarrier;
        List<Singleton> list;

        public Worker(CyclicBarrier cyclicBarrier, List<Singleton> list){
            this.cyclicBarrier =cyclicBarrier;
            this.list = list;
        }

        @Override
        public void run() {
            try {
                // 2) 所有线程阻塞在这
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 3) 满足条件所有线程添加singleton
            list.add(Singleton.getInstance());
        }
    }

    public void test() throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        List<Singleton> list = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 10; i++) {
            new Thread(new Worker(cyclicBarrier, list)).start();
        }
        // 1) 主线程阻塞，让其他线程运行
        Thread.sleep(1000);
        
        for (int i = 1; i < 10; i++) {
            System.out.println(list.get(i-1) == list.get(i));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SingetonTest singetonTest = new SingetonTest();
        singetonTest.test();
    }
}
~~~

