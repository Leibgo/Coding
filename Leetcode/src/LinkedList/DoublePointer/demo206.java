package LinkedList.DoublePointer;

import LinkedList.ListNode;

/**
 * 反转链表
 */
public class demo206 {
    //头插法[单指针]
    public ListNode reverseList(ListNode head) {
        if(head == null || head.next == null){
            return head;
        }
        ListNode cur = head;
        while(head.next != null){
            ListNode memo = head.next.next;
            head.next.next = cur;
            cur  = head.next;
            head.next = memo;
        }
        return cur;
    }
    //双指针
    public ListNode reverseList2(ListNode head) {
        ListNode pre = null;
        ListNode cur = head;
        while(cur != null){
            ListNode memo = cur.next;
            cur.next = pre;
            pre = cur;
            cur = memo;
        }
        return pre;
    }
}
