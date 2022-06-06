import java.util.List;
import java.util.PriorityQueue;

public class demo143 {
    static class ListNode{



        private int val;
        private ListNode next;
        public ListNode(int val){
           this.val = val;
        }
        public ListNode(int val, ListNode next){
            this.val = val;
            this.next = next;
        }
    }
    public static void main(String[] args) {
        PriorityQueue<Integer> que = new PriorityQueue<>();
        que.offer(null);
        ListNode node1 = new ListNode(1);
        ListNode node2 = new ListNode(2);
        ListNode node3 = new ListNode(3);
        ListNode node4 = new ListNode(4);
        ListNode node5 = new ListNode(5);
        node1.next = node2;
        node2.next = node3;
        node3.next = node4;
        node4.next = node5;
        reorderList(node1);
    }
    public static void reorderList(ListNode head) {
        if(head == null || head.next == null){
            return;
        }
        ListNode cur = head.next;
        while(cur != null){
            // 反转链表
            head.next = reverse(cur);
            // 更新节点
            cur = head.next;
            head = head.next;
        }
    }
    // 反转链表
    public static ListNode reverse(ListNode head){
        if(head == null || head.next == null){
            return head;
        }
        ListNode cur = head;
        while(head.next != null){
            ListNode memo = head.next.next;
            head.next.next = cur;
            cur = head.next;
            head.next = memo;
        }
        return cur;
    }
}
