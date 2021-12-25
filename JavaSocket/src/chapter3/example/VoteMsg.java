package chapter3.example;

public class VoteMsg {
    //true为查询 false为投票
    private boolean isInquiry;
    //true为服务器的响应
    private boolean isResponse;
    //候选人ID[0,1000]
    private int candidateId;
    //候选人总票数,只有在响应时才非0
    private long voteCount;

    public static final int MAX_CANDIDATE_ID = 1000;

    public VoteMsg(boolean isInquiry, boolean isResponse, int candidateId, long voteCount) {
        if(voteCount != 0 && !isResponse){
            throw new IllegalArgumentException("请票时,候选人总票数必须为0");
        }
        if(candidateId < 0 || candidateId > MAX_CANDIDATE_ID){
            throw new IllegalArgumentException("候选人ID不符合要求");
        }
        if(voteCount < 0){
            throw new IllegalArgumentException("总票数必须大于等于0");
        }
        this.isInquiry = isInquiry;
        this.isResponse = isResponse;
        this.candidateId = candidateId;
        this.voteCount = voteCount;
    }

    public boolean isInquiry() {
        return isInquiry;
    }

    public void setInquiry(boolean inquiry) {
        isInquiry = inquiry;
    }

    public boolean isResponse() {
        return isResponse;
    }

    public void setResponse(boolean response) {
        isResponse = response;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        if(candidateId < 0 || candidateId > MAX_CANDIDATE_ID){
            throw new IllegalArgumentException("候选人ID不符合要求");
        }
        this.candidateId = candidateId;
    }

    public long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(long voteCount) {
        if(voteCount < 0){
            throw new IllegalArgumentException("总票数必须大于等于0");
        }
        this.voteCount = voteCount;
    }

    @Override
    public String toString() {
        return "VoteMsg{" +
                "isInquiry=" + isInquiry +
                ", isResponse=" + isResponse +
                ", candidateId=" + candidateId +
                ", voteCount=" + voteCount +
                '}';
    }
}
