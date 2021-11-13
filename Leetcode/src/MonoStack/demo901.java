package MonoStack;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class demo901 {
    List<Integer> history = new ArrayList<>();
    Deque<Integer> stack = new LinkedList<>();
    public demo901() {

    }
    //找到左侧第一个比自己大的股票
    public int next(int price) {
        history.add(price);
        int res = 0;
        while(!stack.isEmpty() && price >= history.get(stack.peek())){
            stack.pop();
        }
        res = history.size() - (stack.isEmpty() ? -1 : stack.peek()) - 1;
        stack.push(history.size()-1);
        return res;
    }
}
