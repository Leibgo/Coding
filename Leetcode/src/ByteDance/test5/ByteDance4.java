package ByteDance.test5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ByteDance4 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt(); // 间谍数
        int[][] g = new int[n][n]; // 邻接矩阵
        int inf = Integer.MAX_VALUE / 2;
        for(int i = 0; i < n; i++){
            Arrays.fill(g[i], inf);
        }
        sc.nextLine();
        for(int i = 0; i < n; i++){
            // "2 2 5"
            String[] strs = sc.nextLine().split(" ");
            // 转为整数数组
            int[] data = Arrays.stream(strs).mapToInt(Integer::parseInt).toArray();
            // 更新邻接矩阵
            for(int j = 0; j < data[0]; j++) {
                int target = data[j + 1] - 1;
                g[i][target] = 1;
            }
        }
        int res = Integer.MAX_VALUE;
        for(int i = 0; i < n; i++){
            boolean[] isUsed = new boolean[n];
            int[] distance = new int[n];
            Arrays.fill(distance, inf);
            // B国先通知到的成员
            distance[i] = 0;
            for(int j = 0; j < n; j++){
                int x = -1;
                // 找到最短路径的成员
                for(int k = 0; k < n; k++){
                    if(!isUsed[k] && (x == -1 || distance[k] < distance[x])){
                        x = k;
                    }
                }
                isUsed[x] = true;
                // 更新成员
                for(int m = 0; m < n; m++){
                    distance[m] = Math.min(distance[m], distance[x] + g[x][m]);
                }
            }
            // 计算出当最先通知i这个间谍时至少需要的时间
            int ans = -1;
            for(int h = 0; h < n; h++){
                ans = Math.max(ans, distance[h]);
            }
            if(ans != inf){
                res = Math.min(res, ans);
            }
        }
        if(res == Integer.MAX_VALUE){
            System.out.println(-1);
        }else{
            System.out.println(res);
        }
    }
}
