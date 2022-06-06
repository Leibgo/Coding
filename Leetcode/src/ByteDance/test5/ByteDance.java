package ByteDance.test5;

import java.util.Scanner;

public class ByteDance {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt(); // 总页数
        int m = sc.nextInt(); // 总方案数
        int k = sc.nextInt(); // 至少的间隔数
        int[] res = new int[m];
        sc.nextLine();
        for(int i = 0; i < m; i++){
            String[] ads = sc.nextLine().split(" ");
            int[] ad = new int[ads.length];
            for(int j = 0; j < n; j++){
                ad[j] = Integer.parseInt(ads[j], k);
            }
            res[i] = checkIsProper(ad, k);
        }
        for(int i = 0; i < m; i++){
            System.out.println(res[i]);
        }
    }
    public static int checkIsProper(int[] ad, int cap){
        for(int i = 0; i < ad.length; i++){
            if(ad[i] == 1){
                for(int j = i+1; j < ad.length; j++){
                    if(ad[j] == 1){
                        if(j - i - 1 < cap){
                            return 0;
                        }
                    }
                }

            }
        }
        return 1;
    }

}
