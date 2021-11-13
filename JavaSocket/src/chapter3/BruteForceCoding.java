package chapter3;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//将byte、short、int、long四个整数放到字节数组
public class BruteForceCoding {
    private static byte byteVal = 101;
    private static short shortVal = 10001;
    private static int intVal = 100000001;
    private static long longVal = 1000000000001L;

    //byte、short、int、long所占的字节数
    private final static int BSIZE = Byte.SIZE / Byte.SIZE;   //1
    private final static int SSIZE = Short.SIZE / Byte.SIZE;  //2
    private final static int ISIZE = Integer.SIZE / Byte.SIZE;//3
    private final static int LSIZE = Long.SIZE / Byte.SIZE;   //4
    //掩码(8位)
    private final static int BYTEMASK = 0xFF;
    //字节数组转为无符号数十进制字符串(-128 => 128, -64 => 192)
    public static String byteArrayToDecimalString(byte[] bArray){
        StringBuilder rtn = new StringBuilder();
        for (byte b : bArray) {
            rtn.append(b & BYTEMASK).append(" ");
        }
        return rtn.toString();
    }
    //根据BigEndian编码,高位数值位于内存低端，将long类型的十进制数编码成字节数组
    public static int encodeIntBigEndian(byte[] dst, long val, int offset, int size){
        //将需要的字节移到低8位
        for(int i = 0; i < size; i++){
            dst[offset++] = (byte) (val >> ((size - i - 1) * Byte.SIZE));
        }
        return offset;
    }
    //根据BigEndian解码,将字节数组解码成long类型的十进制数
    public static long decodeIntBigEndian(byte[] val, int offset, int size){
        //因为内存低位存放的是数据高位,每次迭代左移再遇上下一个字节
        long rtn = 0;
        for(int i = 0; i < size; i++){
            rtn = (rtn << Byte.SIZE) | ((long) val[offset + i] & BYTEMASK);
        }
        return rtn;
    }

    public static void main(String[] args) throws IOException {
        //将数值编码进message字节数组中
        byte[] message = new byte[BSIZE + SSIZE + ISIZE + LSIZE];
        int offset = encodeIntBigEndian(message, byteVal, 0, BSIZE);
        offset = encodeIntBigEndian(message, shortVal, offset, SSIZE);
        offset = encodeIntBigEndian(message, intVal, offset, ISIZE);
        encodeIntBigEndian(message, longVal, offset, LSIZE);
        System.out.println("Encoded Message: " + byteArrayToDecimalString(message));
        //从字节数组中将数值解码
        long value = decodeIntBigEndian(message, 0, BSIZE);
        System.out.println("Decoded byte = " + value);
        value = decodeIntBigEndian(message, BSIZE, SSIZE);
        System.out.println("Decoded short = " + value);
        value = decodeIntBigEndian(message, BSIZE + SSIZE, ISIZE);
        System.out.println("Decoded int = " + value);
        value = decodeIntBigEndian(message, BSIZE+SSIZE+ISIZE, LSIZE);
        System.out.println("Decoded long = " + value);
        //转换的危险性[Java默认都是有符号数]
        //如果希望得到无符号数，则将解码结果存入更长的基本整形(byte->short中)
        //如果希望得到有符号数，则放入刚好占用N个字节的基本整形中(byte->byte)
        offset = 4;
        value = decodeIntBigEndian(message, offset, BSIZE);
        System.out.println(value);
        byte bvalue = (byte) decodeIntBigEndian(message, offset, BSIZE);
        System.out.println(bvalue);
    }

}
