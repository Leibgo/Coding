package chapter3.example;

import java.io.*;

/**
 * 二进制格式编码
 */
public class VoteMsgBinCoder implements VoteMsgCode{
    //编码常数
    public static final int MIN_WIRE_LENGTH = 4;
    public static final int MAX_WIRE_LENGTH = 16;
    public static final int MAGIC = 0x5400;
    public static final int MAGIC_MASK = 0xfc00;
    public static final int MAGIC_SHIFT = 8;
    public static final int RESPONSE_FLAG = 0x0200;
    public static final int INQUIRE_FLAG = 0x0100;

    @Override
    public byte[] toWire(VoteMsg msg) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);
        short magicAndFlags = MAGIC;
        if(msg.isInquiry()){
            magicAndFlags |= INQUIRE_FLAG;
        }
        if(msg.isResponse()){
            magicAndFlags |= RESPONSE_FLAG;
        }
        out.write(magicAndFlags);
        out.writeShort(msg.getCandidateId());
        if(msg.isResponse()){
            out.writeLong(msg.getVoteCount());
        }
        out.flush();
        //将输出流缓冲区里的内容复制到data
        byte[] data = byteStream.toByteArray();
        return data;
    }

    @Override
    public VoteMsg fromWire(byte[] input) throws IOException {
        if(input.length < MIN_WIRE_LENGTH){
            throw new IOException("Runt message");
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        DataInputStream in = new DataInputStream(bis);
        int magic = in.readShort();
        if((magic & MAGIC_MASK) != MAGIC){
            throw new IOException("魔法数错误:非法数据");
        }
        boolean resp = ((magic & RESPONSE_FLAG) != 0);
        boolean inq = ((magic & INQUIRE_FLAG) != 0);
        int candidateId = in.readShort();
        if(candidateId < 0 || candidateId > 1000){
            throw new IOException("候选人Id异常");
        }
        long count = 0;
        if(resp){
            count = in.readLong();
            if(count < 0){
                throw new IOException("票数异常");
            }
        }
        //忽略其余字节
        return new VoteMsg(resp,inq,candidateId,count);
    }
}
