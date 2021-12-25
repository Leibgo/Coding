package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo203 {
    public ListNode removeElements(ListNode head, int val) {
        ListNode dummy = new ListNode(0, head);
        ListNode slow = dummy;
        ListNode fast = head;
        while(fast != null){
            if(fast.val != val){
                slow = slow.next;
                fast = fast.next;
            }else{
                slow.next = fast.next;
                fast = fast.next;
            }
        }
        return dummy.next;
    }
}
