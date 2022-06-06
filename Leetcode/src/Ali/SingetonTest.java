package Ali;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;

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
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            list.add(Singleton.getInstance());
        }
    }

    public void test() throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        List<Singleton> list = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 11; i++) {
            new Thread(new Worker(cyclicBarrier, list)).start();
        }

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
