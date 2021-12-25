package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo82 {
    public ListNode deleteDuplicates(ListNode head) {
        ListNode dummy = new ListNode(-101, head);
        ListNode pre = dummy;
        ListNode slow = dummy;
        ListNode fast = head;
        while(fast != null){
            //如果两个节点相同
            if(fast.val == slow.val){
                //先找到第一个值不同的节点
                while(fast != null && fast.val == slow.val){
                    fast = fast.next;
                }
                //移动pre到slow前
                while(pre.next.val != slow.val){
                    pre = pre.next;
                }
                //删除slow-fast之间的节点
                pre.next = fast;
                slow = pre;
            }
            //相同则移动slow、fast两点
            else{
                slow = slow.next;
                fast = fast.next;
            }
        }
        return dummy.next;
    }
}
