package MeiTuan;

import java.util.Scanner;

public class bag {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] nums = new int[n];
        for(int i = 0; i < n; i++){
            nums[i] = sc.nextInt();
        }
        // dp[i][j]:第i个任务分配到第j个任务器时执行任务的耗时总和
        int[][] dp = new int[n+1][3];
        dp[0][0] = nums[0];
        dp[1][1] = nums[1];
        dp[2][2] = nums[2];

        for(int i = 3; i < n; i++){
            dp[i][0] = dp[i-1][0] + nums[i];
            dp[i][1] = dp[i-1][1] + nums[i];
            dp[i][2] = dp[i-1][2] + nums[i];

        }

    }
}
