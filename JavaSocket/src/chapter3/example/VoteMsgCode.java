package chapter3.example;

import java.io.IOException;

/**
 * 对投票消息的序列化和反序列化:将对象->字节数组 || 字节数组->对象
 */
public interface VoteMsgCode {
    //根据特定的协议,将投票消息序列化成字节序列
    byte[] toWire(VoteMsg msg) throws IOException;
    //根据相同的协议,对给定的字节序列进行反序列化,并根据消息的内容构造出消息类的一个实例
    VoteMsg fromWire(byte[] input) throws IOException;
}
