package MonoStack;

import java.util.Deque;
import java.util.LinkedList;

public class demo739 {
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] res = new int[n];
        Deque<Integer> stack = new LinkedList<>();
        //比当前温度更大的下一个温度
        for(int i = n-1; i >= 0; i--){
            int temp = temperatures[i];
            while(!stack.isEmpty() && temp >= temperatures[stack.peek()]){
                stack.pop();
            }
            res[i] = stack.isEmpty() ? 0 : stack.peek() - i;
            stack.push(i);
        }
        return res;
    }
}
