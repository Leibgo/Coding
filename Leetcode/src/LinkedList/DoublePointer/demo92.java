package LinkedList.DoublePointer;

import LinkedList.ListNode;

/**
 * 反转链表II
 */
public class demo92 {
    public ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode dummy = new ListNode(-1,head);
        ListNode preLeft = dummy;
        ListNode pre = head;
        //1.先将pre移到left位置
        //preLeft:为left位置的前一个节点
        for(int i = 1; i < left; i++){
            preLeft = preLeft.next;
            pre = pre.next;
        }
        //stamp节点用于记录第一个反转的位置
        ListNode reverseHead = pre;
        int count = 0;
        ListNode cur = pre.next;
        //反转中间的链表
        while(count < right-left && cur != null){
            ListNode memo = cur.next;
            cur.next = pre;
            pre = cur;
            cur = memo;
            count++;
        }
        preLeft.next = pre;
        //这行代码解决了指针冲突问题和连接问题
        reverseHead.next = cur;
        return dummy.next;
    }
}
