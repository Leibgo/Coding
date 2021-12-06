package LinkedList.DoublePointer;

public class demo430 {
    class Node {
        public int val;
        public Node prev;
        public Node next;
        public Node child;
    }
    public Node flatten(Node head) {
        if(head == null){
            return null;
        }
        Node cur = head;
        while(cur != null){
            //如果child节点不为空,扁平化处理
            if(cur.child != null){
                //扁平化原先的next的节点
                Node postNode = flatten(cur.next);
                Node child = flatten(cur.child);
                //拼接原先的next一段,将它拼接到child后
                Node temp = child;
                while(temp.next != null){
                    temp = temp.next;
                }
                if(postNode != null){
                    temp.next = postNode;
                    postNode.prev = temp;
                }
                //拼接child段
                cur.next = child;
                child.prev = cur;
                cur.child = null;
            }
            cur = cur.next;
        }
        return head;
    }
}
