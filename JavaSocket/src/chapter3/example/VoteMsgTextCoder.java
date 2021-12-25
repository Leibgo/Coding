package chapter3.example;

import java.io.*;
import java.util.Scanner;

public class VoteMsgTextCoder implements VoteMsgCode{

    public static final String MAGIC = "Voting";
    public static final String VOTESTR = "v";
    public static final String INQSTR = "i";
    public static final String RESPONSE = "R";

    public static final String CHARSETNAME = "US-ASCII";
    public static final String DELIMSTR = " ";
    public static final int MAX_WIRE_LENGTH = 2000;

    //将VoteMsg基于文本的方式转换成字节数组
    @Override
    public byte[] toWire(VoteMsg msg) throws IOException {
        String msgString = MAGIC + DELIMSTR + (msg.isInquiry() ? INQSTR : VOTESTR)
                + DELIMSTR +(msg.isResponse() ? RESPONSE + DELIMSTR : "")
                + msg.getCandidateId() + DELIMSTR
                + msg.getVoteCount();
        byte[] data = msgString.getBytes(CHARSETNAME);
        return data;
    }

    //将字节序列转换成VoteMsg
    @Override
    public VoteMsg fromWire(byte[] message) throws IOException {
        ByteArrayInputStream msgStream = new ByteArrayInputStream(message);
        Scanner s = new Scanner(new InputStreamReader(msgStream, CHARSETNAME));
        boolean isInquiry;
        boolean isResponse;
        int candidateId;
        long voteCount;
        String token;
        try{
            token = s.next();
            if(!token.equals(MAGIC)){
                throw new IOException("Bad Magic String");
            }
            token = s.next();
            if(token.equals(VOTESTR)){
                isInquiry = false;
            }
            else if(!token.equals(INQSTR)){
                throw new IOException("Bad vote/inq indicator");
            }
            else{
                isInquiry = true;
            }

            token = s.next();
            if(token.equals(RESPONSE)){
                isResponse = true;
                token = s.next();
            }else{
                isResponse = false;
            }
            //当前token是候选人ID
            candidateId = Integer.parseInt(token);
            if(isResponse){
                token = s.next();
                voteCount = Long.parseLong(token);
            }else{
                voteCount = 0;
            }
        }catch (IOException e){
            throw new IOException("Parse Error...");
        }
        return new VoteMsg(isResponse, isInquiry, candidateId, voteCount);
    }
}
