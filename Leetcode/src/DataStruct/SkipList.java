package DataStruct;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

public class SkipList<T> {
    public static void main(String[] args) {
        SkipList<Integer>list = new SkipList<Integer>();
        for(int i=1;i<20;i++)
        {
            list.insert(i,666);
        }
        list.printSkipList();
        list.delete(4);
        list.delete(8);
        System.out.println("======================================================================================================");
        list.printSkipList();
    }
    // 头结点
    Node headNode;
    // 当前跳表的层数
    int highLevel;
    // 跳表的最高层数
    static final int MAX_LEVEL = 32;
    Random random;

    public SkipList() {
        this.headNode = new Node(Integer.MIN_VALUE,  null);
        this.highLevel = 0;
        this.random = new Random();
    }

    static class Node<T>{
        int key;
        T val;
        // 右下方向的两个指针
        Node down;
        Node right;
        public Node(int key, T val) {
            this.key = key;
            this.val = val;
        }
    }

    // 搜索某个节点
    public Node search(int key){
        Node cur = headNode;
        while(cur != null){
            if(cur.key == key){
                return cur;
            }
            // 当前层的节点右侧已经没有节点,向下转移
            else if(cur.right == null){
                cur = cur.down;
            }
            // 当前层的节点的右节点值大于当前节点的值,向下转移
            else if(cur.right.key > key){
                cur = cur.down;
            }
            // 向右转移
            else{
                cur = cur.right;
            }
        }
        return null;
    }

    // 删除某个节点(该节点可能多层都有)
    public void delete(int key){
        Node cur = headNode;
        while(cur != null){
            if(cur.right == null){
                cur = cur.down;
            }
            else if(cur.right.key == key){
                cur.right = cur.right.right;
                cur = cur.down;
            }
            else if(cur.right.key > key){
                cur = cur.down;
            }
            else{
                cur = cur.right;
            }
        }
    }

    // 插入某个节点
    // 插入节点时，你需要确定是否需要在它的上一层也添加索引,使用random随机数随机决定
    public void insert(int key, T value){
        // 如果插入的节点已经存在,则直接返回
        if(search(key) != null){
            return;
        }
        Node cur = headNode;
        Node downNode = null;
        int level = 1;
        // 栈存储待插入的节点
        Deque<Node> stack = new LinkedList<>();
        // 找到最底层插入节点的合适位置
        while(cur != null){
            if(cur.right == null){
                stack.push(cur);
                cur = cur.down;
            }
            else if(cur.right.key < key){
                cur = cur.right;
            }
            else if(cur.right.key > key){
                stack.push(cur);
                cur = cur.down;
            }
        }
        // 插入节点，并决定是否在上一层创建节点
        while(!stack.isEmpty()){
            Node node = stack.pop();
            Node temp = new Node(key,value);
            temp.right = node.right;
            node.right = temp;
            temp.down = downNode;
            downNode = temp;
            // 当前层数已达到跳表允许的最高层数,退出
            if(level > MAX_LEVEL){
                break;
            }
            // 随机一个概率
            double percent = random.nextDouble();
            // 概率小于50%,退出
            if(percent < 0.5){
                break;
            }
            level++;
            // 如果需要在新的一层创建索引,重置头结点
            if(level > highLevel){
                highLevel = level;
                Node nHead = new Node(Integer.MIN_VALUE, null);
                nHead.down = headNode;
                headNode = nHead;
                stack.push(headNode);
            }
        }
    }

    // 打印跳表
    public void printSkipList(){
        Node teamNode=headNode;
        Node last=teamNode;
        while (last.down!=null){
            last=last.down;
        }
        while (teamNode != null) {
            Node enumNode = teamNode.right;
            Node enumLast = last.right;
            System.out.printf("%-8s", "head->");
            while (enumLast != null && enumNode != null) {
                if (enumLast.key == enumNode.key) {
                    System.out.printf("%-5s", enumLast.key + "->");
                    enumLast = enumLast.right;
                    enumNode = enumNode.right;
                } else {
                    enumLast = enumLast.right;
                    System.out.printf("%-5s", "");
                }

            }
            teamNode = teamNode.down;
            System.out.println();
        }
    }
}
