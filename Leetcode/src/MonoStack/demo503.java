package MonoStack;

import java.util.Deque;
import java.util.LinkedList;

public class demo503 {
    public int[] nextGreaterElements(int[] nums) {
        Deque<Integer> stack = new LinkedList<>();
        int[] res = new int[nums.length];
        //如何循环，复制数组[3,-2,-1,3,-2,-1]
        //利用单调栈，得到循环数组的第一个比它的数
        for(int i = 2*nums.length - 1; i >= 0; i--){
            int num = nums[i % nums.length];
            while(!stack.isEmpty() && num >= stack.peek()){
                stack.pop();
            }
            res[i%nums.length] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(num);
        }
        return res;
    }
}
