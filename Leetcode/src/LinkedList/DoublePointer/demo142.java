package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo142 {
    public ListNode detectCycle(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;
        //证明了环形列表,且保存slow
        while(true){
            //非环
            if(fast == null || fast.next == null){
                return null;
            }
            slow = slow.next;
            fast = fast.next.next;
            if(slow == fast){
                break;
            }
        }
        //从head和slow出发,最终的汇合点就是环形链表的起点
        ListNode temp = head;
        while(slow != null && temp != slow){
            temp = temp.next;
            slow = slow.next;
        }
        return slow;
    }
}
