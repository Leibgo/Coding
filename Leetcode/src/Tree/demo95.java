package Tree;

import java.util.ArrayList;
import java.util.List;

public class demo95 {
    public static void main(String[] args) {
        demo95 d = new demo95();
        d.generateTrees(8);
    }
    public List<TreeNode> generateTrees(int n) {
        if(n == 0){
            return new ArrayList<>();
        }
        return buildTree(1, n);
    }
    public List<TreeNode> buildTree(int start, int end){
        List<TreeNode> res = new ArrayList<>();
        if(start > end){
            return res;
        }
        for(int i = start; i <= end; i++){
            TreeNode root = new TreeNode(i);
            List<TreeNode> lefts =  buildTree(start, i-1);
            List<TreeNode> rights = buildTree(i+1, end);
            for(TreeNode left : lefts){
                for(TreeNode right : rights){
                    root.left = left;
                    root.right = right;
                    res.add(root);
                }
            }
        }
        return res;
    }
}
