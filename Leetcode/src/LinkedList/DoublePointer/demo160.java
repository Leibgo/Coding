package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo160 {
    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        ListNode pointer1 = headA;
        ListNode pointer2 = headB;
        while(true){
            //没有相交节点
            if(pointer1 == null && pointer2 == null){
                return null;
            }
            //如果有一个链表遍历到末尾时,遍历该链表的指针移动到另一链表的起点再遍历
            if(pointer1 == null){
                pointer1 = headB;
            }
            if(pointer2 == null){
                pointer2 = headA;
            }
            if(pointer1 == pointer2){
                return pointer1;
            }
            pointer1 = pointer1.next;
            pointer2 = pointer2.next;
        }
    }
}
