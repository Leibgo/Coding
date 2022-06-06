import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestList {
    public static void main(String[] args) {
//        List<Integer> list = new ArrayList<>();
//        list.add(0);
//        list.add(1);
//        list.add(2);
//        list.add(3);
//        list.add(4);
//        list.add(5);
//        for(int i = 0; i < list.size(); i++){
//            int num = list.get(i);
//            System.out.println("列表大小" + list.size());
//            if(num > 2){
//                list.remove(i);
//                System.out.println("移除元素后的列表大小" + list.size());
//            }
//        }

//        for (Integer integer : list) {
//            if(integer > 2){
//                list.remove(2);
//            }
//        }

//        list.removeIf(integer -> integer > 2);

//        CopyOnWriteArrayList<Integer> list2 = new CopyOnWriteArrayList<>();
//        list2.add(0);
//        list2.add(1);
//        list2.add(2);
//        list2.add(3);
//        list2.add(4);
//        for (Integer integer : list2) {
//            if(integer > 2){
//                list2.remove(integer);
//            }
//        }

        Map<String, String> map = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            map.put("key:"+i, "value:"+i);
        }
        map.put("key:13", "value:13");
    }
}
