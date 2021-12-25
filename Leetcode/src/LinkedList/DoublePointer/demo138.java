package LinkedList.DoublePointer;

import LinkedList.Node;

import java.util.HashMap;
import java.util.Map;

public class demo138 {
    Map<Node, Node> cachedMap = new HashMap<>();
    public Node copyRandomList(Node head) {
        if(head == null){
            return null;
        }
        if(!cachedMap.containsKey(head)){
            Node newHead = new Node(head.val);
            cachedMap.put(head, newHead);
            newHead.next = copyRandomList(head.next);
            newHead.random = copyRandomList(head.random);
        }
        return cachedMap.get(head);
    }
}
