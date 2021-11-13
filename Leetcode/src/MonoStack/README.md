## 单调栈
### 单调栈的特点
单调栈仍旧具有栈"先进后出"(First IN LAST OUT)的特点,
那么单调栈与普通的栈有什么区别呢?
唯一的区别就是单调栈从栈首到栈尾的元素值是单调递增或单调递减的,
而对于普通的栈,栈内的元素不具备单调性.
### 适合的题型
- 1.查找右侧**第一个**比自己大的元素
- 2.查找右侧**第一个**比自己小的元素
- 3.查找左侧**第一个**比自己大的元素
- 4.查找左侧**第一个**比自己小的元素

单调栈的元素不仅可以存放值,也可以存放索引.根据题意自己选择.

查找右侧,从后往前遍历.
查找左侧,从前往右遍历.

标准模板
~~~java
//查找右侧第一个比当前元素大的元素
public class Solution {
    //单调栈标准模板
    public void method(int[] nums) {
        Deque<Integer> stack = new LinkedList<>();
        int[] res = new int[nums.length];
        //选择其他类型的数据结构记录比当前元素大的右侧下一个元素..
        //一般使用Map
        Map<Integer, Integer> map = new HashMap<>();
        for(int i = nums.length - 1; i >= 0; i--){
            int num = nums[i];
            while(!stack.isEmpty() && num > stack.peek()){
                stack.pop();
            }
            map.put(num, stack.isEmpty() ? -1 : stack.peek());
            stack.push(num);
        }
    }
}
~~~
### 比较
普通的迭代遍历也可以解决这种题型,但单调栈的特点帮可以减少遍历次数.
当需要为多个元素查找时节约的时间更多.