package ConcurrentHashMap;

import java.util.concurrent.ConcurrentHashMap;

import static sun.misc.PostVMInitHook.run;

public class test {
    public static void main(String[] args) {
        Integer.numberOfLeadingZeros(16);
        // 最终的容量为64 扩容阈值为48
        ConcurrentHashMap map = new ConcurrentHashMap(32);
        for (int i = 0; i < 47; i++) {
            map.put("abc"+i, i);
        }

        // 添加到48个元素，开始扩容
        new Thread(
                () -> {
                    map.put("通话","11");
                    System.out.println("-----------------------");
                }
        ).start();

        new Thread(
                ()->{
                    map.put("重地", "22");
                    System.out.println("----------------------");
                }
        ).start();

        // 这个线程是为了验证添加元素时，发现对象的hash为MOVED，就协助扩容
        new Thread(
                () -> {
                    map.put("abc5", "xx");
                    System.out.println("-----------------------");
                }
        ).start();
    }
}
