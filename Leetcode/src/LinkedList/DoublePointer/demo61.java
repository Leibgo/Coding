package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo61 {
    public ListNode rotateRight(ListNode head, int k) {
        if(head == null || head.next == null){
            return head;
        }
        int count = 0;
        ListNode temp = head;
        //节点的总数量
        while(temp != null){
            count++;
            temp = temp.next;
        }
        //k大于最大节点,需要转换
        k = k % count;
        if(k == 0 || k == count){
            return head;
        }
        ListNode dummy = new ListNode(-1, head);
        ListNode pre = dummy;
        ListNode cur = head;
        //向右移动k个节点,意味着头结点向右移动count-k次
        for(int i = 0; i < count - k; i++){
            pre = pre.next;
            cur = cur.next;
        }
        //断掉新的头结点与前面节点的关系
        pre.next = null;
        ListNode newHead = cur;
        //找到最后一个节点
        while(cur.next != null){
            cur = cur.next;
        }
        //拼接到之前的头结点
        cur.next = head;
        return newHead;
    }
}
