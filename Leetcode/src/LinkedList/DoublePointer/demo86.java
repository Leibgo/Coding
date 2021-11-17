package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo86 {
    public static void main(String[] args) {
        demo86 d = new demo86();
        ListNode head = new ListNode(1);
        ListNode temp = head;
        temp.next = new ListNode(4);
        temp = temp.next;
        temp.next = new ListNode(3);
        temp = temp.next;
        temp.next = new ListNode(2);
        temp = temp.next;
        temp.next = new ListNode(5);
        temp = temp.next;
        temp.next = new ListNode(2);
        temp = temp.next;
        d.partition(head,3);
    }
    public ListNode partition(ListNode head, int x) {
        ListNode dummy1 = new ListNode(-101);
        ListNode pointer1 = dummy1;
        ListNode dummy2 = new ListNode(-101);
        ListNode pointer2 = dummy2;
        ListNode temp = head;
        while(temp != null){
            //寻找比x小的node节点
            if(temp.val < x){
                pointer1.next = new ListNode(temp.val);
                pointer1 = pointer1.next;
            }
            //寻找比x大的node节点
            else if(temp.val >= x){
                pointer2.next = new ListNode(temp.val);
                pointer2 = pointer2.next;
            }
            temp = temp.next;
        }
        pointer1.next = dummy2.next;
        return dummy1.next;
    }
}
