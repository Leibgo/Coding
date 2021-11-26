package LinkedList.DoublePointer;

import LinkedList.ListNode;

public class demo147 {
    public static void main(String[] args) {
        ListNode n1 = new ListNode(3);
        ListNode n2 = new ListNode(2);
        ListNode n3 = new ListNode(4);
        n1.next = n2;
        n2.next = n3;
        insertionSortList(n1);
    }
    public static ListNode insertionSortList(ListNode head) {
        if(head == null || head.next == null){
            return head;
        }
        ListNode node = head.next;
        ListNode temp = head;
        temp.next = null;
        return helper(temp, node);
    }
    //sortNode:已经排完序的链表
    //node:待插入的节点
    public static ListNode helper(ListNode sortNode, ListNode node){
        if(node == null){
            return sortNode;
        }
        //保存下一个要插入的节点
        ListNode post = node.next;
        node.next = null;
        //节点值小于以及排序链表的第一个值
        if(node.val < sortNode.val){
            node.next = sortNode;
            return helper(node, post);
        }
        //节点值大于排序链表的第一个值
        //1.排序链表目前只有一个节点
        if(sortNode.next == null){
            sortNode.next = node;
            return helper(sortNode, post);
        }
        //插入合适的位置
        ListNode slow = sortNode;
        ListNode fast = sortNode.next;
        while(fast != null){
            if(node.val < fast.val){
                slow.next = node;
                node.next = fast;
                break;
            }
            slow = slow.next;
            fast = fast.next;
        }
        //说明插入节点比链表的最大值
        if(fast == null){
            slow.next = node;
        }
        return helper(sortNode, post);
    }
}
