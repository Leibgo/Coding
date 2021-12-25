package LinkedList.DoublePointer;

import LinkedList.ListNode;

import java.util.HashMap;
import java.util.Map;

public class demo817 {
    public int numComponents(ListNode head, int[] nums) {
        int sz = 0;
        ListNode cur = head;
        Map<Integer, Integer> map = new HashMap<>();
        //计算长度,将元素和对应的位置存入map
        while(cur != null){
            map.put(cur.val, sz);
            cur = cur.next;
            sz++;
        }
        boolean[] rememberSet = new boolean[sz];
        //记录nums中的元素位置
        for(int i = 0; i < nums.length; i++){
            if(map.containsKey(nums[i])){
                rememberSet[map.get(nums[i])] = true;
            }
        }
        //碰到false,就说明出现一个组件
        int res = 0;
        int[] dp = new int[sz];
        dp[0] = rememberSet[0] ? 1 : 0;
        for(int i = 1; i < sz; i++){
            //当前元素是子集中的一个元素
            if(rememberSet[i]){
                if(rememberSet[i-1]){
                    dp[i] = dp[i-1];
                }else{
                    dp[i] = dp[i-1] + 1;
                }
            }else{
                dp[i] = dp[i-1];
            }
        }
        return dp[sz-1];
    }
}
