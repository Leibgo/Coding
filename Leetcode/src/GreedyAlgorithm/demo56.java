package GreedyAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class demo56 {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        Integer[] i = list.toArray(new Integer[0]);
        for (Integer integer : i) {
            System.out.println(integer);
        }

    }
    public int[][] merge(int[][] intervals) {
        //合并所有重叠区间
        //贪心算法
        //将所有数组按照起点升序排序
        Arrays.sort(intervals, new Comparator<int[]>(){
            public int compare(int[] nums1, int[] nums2){
                if(nums1[0] == nums2[0]){
                    return nums2[1] - nums1[1];
                }
                return nums1[0] - nums2[0];
            }
        });
        int n = intervals.length; //列
        List<int[]> resList = new ArrayList<>();
        int start = intervals[0][0];
        int end = intervals[0][1];
        resList.add(new int[]{start, end});
        //实时更新start和end,因为有区间发生合并
        for(int i = 1; i < n; i++){
            int[] cur = intervals[i];
            //如果当前区间的起点小于前一个区间的末尾,则有重合
            if(cur[0] <= end){
                //当前区间的末尾大于前一个区间的末尾,合并
                if(cur[1] > end){
                    int[] merge = new int[]{start, cur[1]};
                    resList.remove(resList.size()-1);
                    resList.add(merge);
                    end = cur[1];
                }
                //如果当前区间的末尾小于前一个区间的末尾,就不需要改变,因为已经包含该区间了
            }else{
                resList.add(cur);
                start = cur[0];
                end = cur[1];
            }
        }
//        int sz = resList.size();
//        int[][] res = new int[sz][2];
//        for(int i = 0; i < sz; i++){
//            res[i] = resList.get(i);
//        }
//        return res;
        return resList.toArray(new int[0][]);
    }
}
