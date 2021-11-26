package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo234 {
    public boolean isPalindrome(ListNode head) {
        if(head == null || head.next == null){
            return true;
        }
        ListNode slow = head;
        ListNode fast = head;
        //找到中间节点
        while(fast != null && fast.next != null){
            slow = slow.next;
            fast = fast.next.next;
        }
        ListNode reverseHead = fast != null ? slow.next : slow;
        ListNode cur = head;
        //反转中间节点前的链表
        while(head.next != slow){
            ListNode memo = head.next.next;
            head.next.next = cur;
            cur = head.next;
            head.next = memo;
        }
        //比较是否是回文串
        while(cur != null && reverseHead != null){
            if(cur.val != reverseHead.val){
                return false;
            }
            cur = cur.next;
            reverseHead = reverseHead.next;
        }
        return true;
    }
}
