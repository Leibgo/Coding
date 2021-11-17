package LinkedList.Classic;

import LinkedList.ListNode;

public class demo876 {
    public ListNode middleNode(ListNode head) {
        int sz = 0;
        ListNode temp = head;
        while(temp != null){
            sz++;
            temp = temp.next;
        }
        //中间节点的位置
        int mid = (sz / 2);
        ListNode cur = head;
        for(int i = 0; i < mid; i++){
            cur = cur.next;
        }
        return cur;
    }
}
