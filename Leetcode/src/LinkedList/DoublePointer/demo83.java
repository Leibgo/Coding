package LinkedList.DoublePointer;

import LinkedList.ListNode;

import java.util.HashSet;
import java.util.Set;

public class demo83 {
    public static ListNode deleteDuplicates(ListNode head) {
        ListNode dummy = new ListNode();
        ListNode cur = dummy;
        ListNode temp = head;
        //记忆集
        Set<Integer> remSet = new HashSet<>();
        while(temp != null){
            //不包含重复的元素
            if(remSet.contains(temp.val)){
                temp = temp.next;
                continue;
            }
            remSet.add(temp.val);
            cur.next = new ListNode(temp.val);;
            cur = cur.next;
            temp = temp.next;
        }
        return dummy.next;
    }
    public ListNode deleteDuplicates1(ListNode head) {
        //快慢指针
        ListNode dummy = new ListNode(-101, head);
        ListNode slow = dummy;
        ListNode fast = head;
        while(fast != null){
            if(fast.val != slow.val){
                slow = slow.next;
                fast = fast.next;
            }else{
                slow.next = fast.next;
                fast = fast.next;
            }
        }
        return dummy.next;
    }
}
