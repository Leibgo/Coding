package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo19 {
    public ListNode removeNthFromEnd(ListNode head, int n) {
        int sz = 1;
        ListNode temp = head;
        //计算链表长度
        while(temp.next != null){
            temp = temp.next;
            sz++;
        }
        if(sz == 1) return null;
        //双指针
        ListNode dummy = new ListNode(-1, head);
        ListNode slow = dummy;
        ListNode fast = head;
        for(int i = 0; i < sz - n; i++){
            slow = slow.next;
            fast = fast.next;
        }
        slow.next = fast.next;
        return dummy.next;
    }
}
