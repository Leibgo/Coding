package chapter4;

public class ThreadExample implements Runnable{
    private String greeting;

    public ThreadExample(String greeting) {
        this.greeting = greeting;
    }

    @Override
    public void run() {
        while (true){
            System.out.println(Thread.currentThread().getName() + ": " + greeting);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(new ThreadExample("Hello"));
        Thread t2 = new Thread(new ThreadExample("你好"));
        Thread t3 = new Thread(new ThreadExample("Ciao"));
        t1.start();
        t2.start();
        t3.start();
    }
}
