package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo25 {
    public ListNode reverseKGroup(ListNode head, int k) {
        if(head == null){
            return null;
        }
        ListNode cur = head;
        for(int i = 1; i < k && cur != null; i++){
            cur = cur.next;
        }
        //反转剩余的节点
        if(cur == null){
            return head;
        }
        //保存下一个节点
        ListNode next = cur.next;
        //切断
        cur.next = null;
        //反转当前链表
        ListNode res = reverse(head);
        //连接
        head.next = reverseKGroup(next, k);
        return res;
    }
    //反转链表
    public ListNode reverse(ListNode head){
        ListNode cur = head;
        while(head.next != null){
            ListNode memo = head.next.next;
            head.next.next = cur;
            cur = head.next;
            head.next = memo;
        }
        return cur;
    }
}
