package MeiTuan;

import java.util.Scanner;

public class Desc {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] nums = new int[n];
        for(int i = 0; i < n; i++){
            nums[i] = sc.nextInt();
        }
        // 单调递增的分界线
        int left = 0;
        // 单调递减的分界线
        int right = n-1;
        // 求单调递增的分界线
        for(int i = 1; i < n; i++){
            if(nums[i] <= nums[i-1]){
                left = i-1;
                break;
            }
        }
        for(int i = n-2; i >= 0; i--){
            if(nums[i] <= nums[i+1]){
                right = i+1;
                break;
            }
        }
        // 分界线相同 返回0
        if(right == left){
            System.out.println(0);
        }
        int sum = 0;
        int target = nums[left];
        // 贪心算法
        for(int i = left + 1; i < right; i++){
            // nums[i]本身大于target
            if(target < nums[i]){
                target = nums[i];
            }
            // 单调递增序列的尾数
            else{
                target++;
                sum += target - nums[i];
                if(i == right - 1){
                    if(nums[i] > nums[right]){
                        break;
                    }else{
                        sum += nums[right] - target + 1;
                    }
                }
            }

        }
        System.out.println(sum);

    }
}
