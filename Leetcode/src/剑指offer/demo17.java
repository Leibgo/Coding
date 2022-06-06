package 剑指offer;

/**
 * 打印从 1-n 的n位数
 */
public class demo17 {
    int n;
    int start;
    int nine;
    char[] loop = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    public static void main(String[] args) {
        demo17 demo17 = new demo17();
        String s = demo17.printNum(2);
        System.out.println(s);
    }
    public String printNum(int n){
        this.n = n;
        // 起始点：子串的起点
        this.start = n-1;
        // ‘9’的个数
        this.nine = 0;
        char[] num = new char[n];
        StringBuilder res = new StringBuilder();
        dfs(num, res, 0);
        res.deleteCharAt(res.length()-1);
        return res.toString();
    }
    public void dfs(char[] num, StringBuilder res, int i){
        if(i == n){
            // 字符串为"00000000001",只取start后面的子串'9'
            String str = String.valueOf(num).substring(start);
            if(!str.equals("0")){
                res.append(str + ",");
            }
            // 子串所有的字符都为'9'，start可以减1进位，从'99' -> '100'
            if(n - start == nine){
                start--;
            }
            return;
        }
        for (char c : loop) {
            if(c == '9'){
                nine++;
            }
            num[i] = c;
            dfs(num, res, i+1);
        }
        nine--;
    }
}
