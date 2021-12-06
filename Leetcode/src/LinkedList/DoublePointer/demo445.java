package LinkedList.DoublePointer;

import LinkedList.ListNode;

import java.util.Deque;
import java.util.LinkedList;

public class demo445 {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        int count1 = 0;
        int count2 = 0;
        //计算链表长度
        ListNode temp1 = l1;
        ListNode temp2 = l2;
        while(temp1 != null){
            temp1 = temp1.next;
            count1++;
        }
        while(temp2 != null){
            temp2 = temp2.next;
            count2++;
        }
        int n = Math.max(count1, count2);
        //用数组表示链表中每个节点的值
        int[] nums1 = new int[n];
        int[] nums2 = new int[n];
        //链表长度不同时,较短的链表的数组从count1-count2开始,在此之前数组中的值都为0
        //第一种情况
        if(count1 > count2){
            int i = count1 - count2;
            while(l2 != null){
                nums2[i] = l2.val;
                l2 = l2.next;
                i++;
            }
            int j = 0;
            while(l1 != null){
                nums1[j] = l1.val;
                l1 = l1.next;
                j++;
            }
        }
        //第二种情况
        else if(count1 < count2){
            int i = count2 - count1;
            while(l1 != null){
                nums1[i] = l1.val;
                l1 = l1.next;
                i++;
            }
            int j = 0;
            while(l2 != null){
                nums2[j] = l2.val;
                l2 = l2.next;
                j++;
            }
        }
        //第三中情况：两个链表长度相同
        else{
            int i = 0;
            while(l1 != null){
                nums1[i] = l1.val;
                nums2[i] = l2.val;
                l1 = l1.next;
                l2 = l2.next;
                i++;
            }
        }
        int[] resArray = new int[n];
        boolean flag = false;
        //两个数组索引值相同的值相加
        for(int i = n-1; i >= 0; i--){
            int val = resArray[i] + nums1[i] + nums2[i];
            if(val >= 10){
                if(i > 0){
                    resArray[i-1] += 1;
                }else{
                    flag = true;
                }
            }
            //进位
            resArray[i] = val % 10;
        }
        //链表连接
        ListNode dummy = new ListNode(0);
        ListNode temp = dummy;
        //进位的特殊情况
        if(flag){
            temp.next = new ListNode(1);
            temp = temp.next;
        }
        for(int i = 0; i < n; i++){
            temp.next = new ListNode(resArray[i]);
            temp = temp.next;
        }
        return dummy.next;
    }
    //使用栈解决
    public ListNode addTwoNumbers2(ListNode l1, ListNode l2) {
        //创建对应于链表的栈
        Deque<Integer> stack1 = new LinkedList<>();
        Deque<Integer> stack2 = new LinkedList<>();
        while(l1 != null){
            stack1.push(l1.val);
            l1 = l1.next;
        }
        while(l2 != null){
            stack2.push(l2.val);
            l2 = l2.next;
        }
        //创建相加后的栈
        Deque<Integer> res = new LinkedList<>();
        boolean flag = false;
        while(!stack1.isEmpty() || !stack2.isEmpty()){
            int val1 = stack1.isEmpty() ? 0 : stack1.pop();
            int val2 = stack2.isEmpty() ? 0 : stack2.pop();
            int sum = val1 + val2;
            if(flag){
                sum += 1;
                flag = false;
            }
            if(sum >= 10){
                flag = true;
            }
            res.push(sum % 10);
        }
        if(flag){
            res.push(1);
        }
        ListNode dummy = new ListNode(-1);
        ListNode temp = dummy;
        //将栈中的结果添加进链表
        while(!res.isEmpty()){
            temp.next = new ListNode(res.pop());
            temp = temp.next;
        }
        return dummy.next;
    }
}
