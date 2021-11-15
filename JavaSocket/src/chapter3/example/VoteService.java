package chapter3.example;

import java.util.HashMap;
import java.util.Map;

/**
 * 投票服务器所用到的方法
 */
public class VoteService {
    private Map<Integer, Long> results = new HashMap<>();

    public VoteMsg handleRequest(VoteMsg msg){
        //如果是响应，则回复
        if(msg.isResponse()){
            return msg;
        }
        msg.setResponse(true);
        //获取候选ID和票数
        int candidate = msg.getCandidateId();
        Long count = msg.getVoteCount();
        if(count == null){
            count = 0L;
        }
        //如果是投票，增加票数
        if(!msg.isInquiry()){
            results.put(candidate, count++);
        }
        msg.setVoteCount(count);
        return msg;
    }
}
