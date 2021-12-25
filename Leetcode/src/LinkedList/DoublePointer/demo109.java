package LinkedList.DoublePointer;

import LinkedList.ListNode;
import LinkedList.TreeNode;

public class demo109 {
    public TreeNode sortedListToBST(ListNode head) {
        if(head == null){
            return null;
        }
        if(head.next == null){
            return new TreeNode(head.val);
        }
        ListNode slow = head;
        ListNode fast = head;
        ListNode dummy = new ListNode(-1, head);
        ListNode pre = dummy;
        while(fast != null && fast.next != null){
            slow = slow.next;
            fast = fast.next.next;
            pre = pre.next;
        }
        TreeNode root = new TreeNode(slow.val);
        pre.next = null;
        root.left = sortedListToBST(head);
        root.right = sortedListToBST(slow.next);
        return root;
    }
}
