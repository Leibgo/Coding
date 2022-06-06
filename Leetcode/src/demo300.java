import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class demo300 {
    /**
     * 最长连续子序列的路径
     * @param args
     */
    public static void main(String[] args) {
        int[] nums = {10,9,2,5,3,7,101,18};
        List<Integer> list = lengthOfLIS(nums);
        System.out.println(list);
    }
    public static List<Integer> lengthOfLIS(int[] nums) {
        int[] dp = new int[nums.length];
        List<List<Integer>> list = new ArrayList<>();
        Arrays.fill(dp, 1);
        int index = 0;
        int res = 1;
        for(int i = 0; i < nums.length; i++){
            List<Integer> temp = new ArrayList<>();
            temp.add(nums[i]);
            list.add(temp);
            for(int j = 0; j < i; j++){
                if(nums[i] > nums[j]){
                    if(dp[i] < dp[j]+1){
                        List<Integer> temp2 = new ArrayList<>(list.get(j));
                        temp2.add(nums[i]);
                        list.remove(i);
                        list.add(temp2);
                        dp[i] = dp[j] + 1;
                    }
                }
            }
            if(dp[i] > res){
                res = dp[i];
                index = i;
            }
        }
        return list.get(index);
    }
}
