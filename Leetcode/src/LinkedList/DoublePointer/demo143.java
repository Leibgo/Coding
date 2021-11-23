package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo143 {
    public static void main(String[] args) {
        ListNode l1 = new ListNode(1);
        ListNode l2 = new ListNode(2);
        ListNode l3 = new ListNode(3);
        ListNode l4 = new ListNode(4);
        l1.next = l2;
        l2.next = l3;
        l3.next = l4;
        reorderList(l1);
    }
    public static void reorderList(ListNode head) {
        ListNode dummy = new ListNode(-1, head);
        ListNode temp = head;
        //计算链表的长度
        int sz = 0;
        while(temp != null){
            temp = temp.next;
            sz++;
        }
        //迭代反转链表
        ListNode beforPre = head;
        ListNode pre = head;
        ListNode reverseHead = pre;
        ListNode cur = pre.next;
        //反转当前start为起点的链表
        for(int i = 1; i < sz - 1; i++){
            reverseHead = pre;
            while(cur != null){
                ListNode memo = cur.next;
                cur.next = pre;
                pre = cur;
                cur = memo;
            }
            beforPre.next = pre;
            reverseHead.next = null;
            pre = pre.next;
            cur = pre.next;
        }
    }
}
