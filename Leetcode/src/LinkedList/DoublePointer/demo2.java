package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo2 {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(0);
        ListNode cur = dummy;
        int p = 0;
        while(l1 != null || l2 != null){
            int val1 = l1 == null ? 0 : l1.val;
            int val2 = l2 == null ? 0 : l2.val;
            int sum = val1 + val2 + p;
            if(sum >= 10){
                p = 1;
            }else{
                p = 0;
            }
            cur.next = new ListNode(sum % 10);
            cur = cur.next;
            l1 = l1 == null ? l1 : l1.next;
            l2 = l2 == null ? l2 : l2.next;
        }
        if(p == 1){
            cur.next = new ListNode(1);
        }
        return dummy.next;
    }
}
