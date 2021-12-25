package LinkedList.DoublePointer;

import LinkedList.ListNode;

/**
 * 环形链表
 * 只需要确定是不是环形链表,而不用确定环形链表的起点
 */
public class demo141 {
    public boolean hasCycle(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;
        //快慢指针
        while(fast != null && fast.next != null){
            slow = slow.next;
            fast = fast.next.next;
            if(slow == fast){
                return true;
            }
        }
        return false;
    }
}
