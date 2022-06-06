package Ali;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class CountDownForMultiThread {


    class Worker implements Runnable{
        CountDownLatch endLatch;
        CountDownLatch beginLatch;
        List<Singleton> list;

        public Worker(CountDownLatch endLatch, CountDownLatch beginLatch, List<Singleton> list){
            this.beginLatch = beginLatch;
            this.endLatch = endLatch;
            this.list = list;
        }

        @Override
        public void run() {
            try {
                beginLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.add(Singleton.getInstance());
            endLatch.countDown();
        }
    }

    public void test() throws InterruptedException {
        CountDownLatch beginLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(10);
        List<Singleton> list = new CopyOnWriteArrayList<>();
        for(int i = 0; i < 10; i++){
            new Thread(new Worker(endLatch, beginLatch, list)).start();
        }
        Thread.sleep(1000);

        beginLatch.countDown();
        endLatch.await();

        System.out.println(list.size());
        for (int i = 1; i < 10; i++) {
            System.out.println(list.get(i-1) == list.get(i));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownForMultiThread countDownForMultiThread = new CountDownForMultiThread();
        countDownForMultiThread.test();
    }
}
