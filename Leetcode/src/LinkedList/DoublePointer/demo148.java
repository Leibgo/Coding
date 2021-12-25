package LinkedList.DoublePointer;

import LinkedList.ListNode;

/**
 * 使用快慢指针对链表进行归并排序
 */
public class demo148 {
    public ListNode sortList(ListNode head) {
        if(head == null || head.next == null){
            return head;
        }
        ListNode dummy = new ListNode(-1, head);
        ListNode pre = dummy;
        ListNode slow = head;
        ListNode fast = head;
        //使用双指针找到中间的节点
        while(fast != null && fast.next != null){
            slow = slow.next;
            fast = fast.next.next;
            pre = pre.next;
        }
        pre.next = null;
        ListNode left = sortList(head);
        ListNode right = sortList(slow);
        return merge(left, right);
    }
    //合并两个链表
    public ListNode merge(ListNode left, ListNode right){
        ListNode dummy = new ListNode(-1);
        ListNode temp = dummy;
        while(left != null && right != null){
            if(left.val < right.val){
                temp.next = left;
                left = left.next;
            }else{
                temp.next = right;
                right = right.next;
            }
            temp = temp.next;
        }
        if(left == null){
            temp.next = right;
        }else{
            temp.next = left;
        }
        return dummy.next;
    }
}
