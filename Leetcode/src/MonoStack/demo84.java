package MonoStack;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class demo84 {
    public int largestRectangleArea(int[] heights) {
        //利用单调栈实现两个要求
        //1.找到左侧比自己小的柱子
        //2.找到右侧比自己小的柱子
        Deque<Integer> stack = new LinkedList<>();
        Map<Integer, Integer> leftMap = new HashMap<>();
        Map<Integer, Integer> rightMap = new HashMap<>();
        //实现1
        for(int i = 0; i < heights.length; i++){
            int h = heights[i];
            while(!stack.isEmpty() && h <= heights[stack.peek()]){
                stack.pop();
            }
            leftMap.put(i, stack.isEmpty() ? -1 : stack.peek());
            stack.push(i);
        }
        //实现2
        Deque<Integer> stack2 = new LinkedList<>();
        for(int i = heights.length - 1; i >= 0; i--){
            int h = heights[i];
            while(!stack2.isEmpty() && h <= heights[stack2.peek()]){
                stack2.pop();
            }
            rightMap.put(i, stack2.isEmpty() ? heights.length : stack2.peek());
            stack2.push(i);
        }
        //计算最大面积
        int maxArea = 0;
        for(int i = 0; i < heights.length; i++){
            maxArea = Math.max(maxArea, (rightMap.get(i) - leftMap.get(i) - 1) * heights[i]);
        }
        return maxArea;
    }
}
