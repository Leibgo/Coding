package LinkedList.DoublePointer;

import LinkedList.ListNode;

import java.util.HashMap;
import java.util.Map;

public class demo1171 {
    public ListNode removeZeroSumSublists(ListNode head) {
        Map<Integer, ListNode> map = new HashMap<>();
        ListNode dummy = new ListNode(0, head);
        int sum = 0;
        //相同的sum记录的是最后一个节点
        for(ListNode cur = dummy; cur != null; cur = cur.next){
            sum += cur.val;
            map.put(sum, cur);
        }
        sum = 0;
        for(ListNode cur = dummy; cur != null; cur = cur.next){
            sum += cur.val;
            cur.next = map.get(sum).next;
        }
        return dummy.next;
    }
}
