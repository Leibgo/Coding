package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo23 {
    public ListNode mergeKLists(ListNode[] lists) {
        if(lists.length == 0){
            return null;
        }
        ListNode res = lists[0];
        for(int i = 1; i < lists.length; i++){
            res = merge(res, lists[i]);
        }
        return res;
    }
    //使用归并排序,合并两个有序链表
    public ListNode merge(ListNode left, ListNode right){
        ListNode head = new ListNode();
        ListNode cur = head;
        while(left != null && right != null){
            if(left.val <= right.val){
                cur.next = left;
                cur = cur.next;
                left = left.next;
            }else{
                cur.next = right;
                cur = cur.next;
                right = right.next;
            }
        }
        if(left == null){
            cur.next = right;
        }
        if(right == null){
            cur.next = left;
        }
        return head.next;
    }
}
