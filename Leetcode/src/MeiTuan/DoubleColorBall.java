package MeiTuan;

import java.util.Scanner;

public class DoubleColorBall {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // n: 桌上球的数量
        int n = sc.nextInt();
        // r: 口袋中红色球的数量
        int r = sc.nextInt();
        // b: 口袋中蓝色球的数量
        int b = sc.nextInt();
        //
        int needRed = 0;
        int needBlue = 0;
        boolean flag =false;
        sc.nextLine();
        // rrbbr
        String s = sc.nextLine();
        StringBuilder sb = new StringBuilder(s);
        for(int i = 1; i < n; i++){
            if(sb.charAt(i) != sb.charAt(i-1)){
                continue;
            }else{
                if(sb.charAt(i) == 'r'){
                    if(b > 0){
                        b--;
                        sb.insert(i, 'b');
                    }else{
                        flag = true;
                        needBlue++;
                    }
                }
                else if(sb.charAt(i) == 'b'){
                    if(r > 0){
                        r--;
                        sb.insert(i, 'r');
                    }else{
                        flag = true;
                        needRed++;
                    }
                }
            }
        }
        if(!flag){
            System.out.println(sb.length());
        }else{
            System.out.println(needRed);
            System.out.println(needBlue);
        }
    }
}
