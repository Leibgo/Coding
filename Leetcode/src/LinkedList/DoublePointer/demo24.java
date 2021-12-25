package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo24 {
    public ListNode swapPairs(ListNode head) {
        if(head == null || head.next == null){
            return head;
        }
        ListNode dummy = new ListNode(-1, head);
        ListNode temp = dummy;
        while(temp.next != null && temp.next.next != null){
            ListNode n1 = temp.next;
            ListNode n2 = temp.next.next;
            temp.next = n2;
            n1.next = n2.next;
            n2.next = n1;
            temp = n1;
        }
        return dummy.next;
    }
}
