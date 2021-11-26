package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo143 {
    public void reorderList(ListNode head) {
        ListNode preHead = head;
        //需要进行多次反转
        //preHead为每次反转链表的前一个节点
        while(preHead != null){
            if(preHead == null || preHead.next == null){
                return;
            }
            //反转链表
            ListNode tempHead = preHead.next;
            ListNode cur = tempHead;
            while(tempHead.next != null){
                ListNode memo = tempHead.next.next;
                tempHead.next.next = cur;
                cur = tempHead.next;
                tempHead.next = memo;
            }
            preHead.next = cur;
            preHead = preHead.next;
        }
    }
}
