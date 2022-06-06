package JUC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FutureTaskDemo {
    static class Task implements Callable<Integer>{
        private String taskName;
        private Integer res;

        public Task(String taskName, Integer res) {
            this.taskName = taskName;
            this.res = res;
        }

        @Override
        public Integer call() throws Exception {
            System.out.println("子线程:" + taskName + " 开始启动了===================by" + " " + Thread.currentThread().getName());
            for(int i = 0; i < 1000; i++){
                res += i;
            }
            // 执行时间为2秒
            Thread.sleep(2000);
            System.out.println("子线程:" + taskName + " 执行完毕by" + " " + Thread.currentThread().getName());
            return res;
        }
    }

    // 主线程执行自己的任务，多线程执行计算任务,过一阵时间后主线程去取数据
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 线程池：5个线程
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        List<FutureTask> taskList = new ArrayList<>();
        // 10个任务
        for(int i = 0; i < 10; i++){
            FutureTask<Integer> futureTask = new FutureTask<>(new Task("任务:" + i, 0));
            taskList.add(futureTask);
            threadPool.submit(futureTask);
        }
        // 线程池执行这些任务
        System.out.println("主线程先做其他事");
        for(int i = 0; i < 1000; i++){

        }
        System.out.println("主线程要获取结果了!!!");
        long begin = System.currentTimeMillis();
        int totalResult = 0;
        for (FutureTask futureTask : taskList) {
            totalResult += (int) futureTask.get();
        }
        long end = System.currentTimeMillis();
        System.out.println("执行时间:" + (end - begin));
        System.out.println(totalResult);
        threadPool.shutdown();

    }
}
