package MonoStack;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 接雨水
 */
public class demo42 {
    public int trap(int[] height) {
        //以当前柱子作为新添加的右侧柱子
        //如果当前柱子小于左侧的柱子(栈顶)的高度,则可以继续注入雨水
        //如果当前柱子大于左侧的柱子(栈顶)的高度,则应该结算左侧雨水的容量
        //如何结算？
        //以栈顶的柱子高度作为底，计算面积
        //              _
        //        _    | |
        //       | |_  |c|
        //       | | |_| |
        //       |_|_|_|_|

        Deque<Integer> stack = new LinkedList<>();
        int totalArea = 0;
        for(int i = 0; i < height.length; i++){
            //当前柱子高度
            int h = height[i];
            while(!stack.isEmpty() && h >= height[stack.peek()]){
                //栈顶柱子高度
                int base = height[stack.peek()];
                stack.pop();
                if(stack.isEmpty()){
                    break;
                }
                totalArea += (Math.min(height[stack.peek()], h) - base) * (i - stack.peek() - 1);
            }
            stack.push(i);
        }
        return totalArea;
    }
}
