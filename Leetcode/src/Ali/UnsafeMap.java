package Ali;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UnsafeMap {
    public static void main(String[] args) throws Exception {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        for (int i = 0; i < 100; i++) {
            map.put("test:"+i, "loubei"+i);
        }
        map.put("test:10", "吴雨晴");
    }
}
