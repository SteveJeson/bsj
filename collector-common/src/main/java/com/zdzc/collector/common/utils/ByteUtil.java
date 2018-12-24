package com.zdzc.collector.common.utils;

import com.zdzc.collector.common.jconst.SysConst;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;

/**
 * @Author liuwei
 * @Description 字节处理工具类
 * @Date 2018/12/11 14:43
 */
public class ByteUtil {

    /**
     * 截取byte数组并转化成int类型
     * @param data
     * @param from
     * @param len
     * @return
     */
    public static int cutBytesToInt(byte[] data, int from, int len){
        byte[] buffer = new byte[len];
        System.arraycopy(data, from, buffer, 0, len);
        return byteToInteger(buffer);
    }

    /**
     * 单个字节转int类型
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/11 15:05
     */
    public static int byteToInteger(byte[] value) {
        int result;
        if (value.length == SysConst.FIGURE_ONE)
        {
            result = oneByteToInteger(value[0]);
        }
        else if (value.length == SysConst.FIGURE_TWO)
        {
            result = twoBytesToInteger(value);
        }
        else if (value.length == SysConst.FIGURE_THREE)
        {
            result = threeBytesToInteger(value);
        }
        else if (value.length == SysConst.FIGURE_FOUR)
        {
            result = fourBytesToInteger(value);
        }
        else
        {
            result = fourBytesToInteger(value);
        }
        return result;
    }

    /**
     * 把一个byte转化位整形,通常为指令用
     * @param value
     * @return
     */
    public static int oneByteToInteger(byte value)
    {
        return (int)value & 0xFF;
    }


    /**
     * 把一个2位的数组转化位整形
     * @param value
     * @return
     */
    public static int twoBytesToInteger(byte[] value) {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        return ((temp0 << 8) + temp1);
    }

    /**
     * 把一个3位的数组转化位整形
     * @param value
     * @return
     */
    public static int threeBytesToInteger(byte[] value) {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        return ((temp0 << 16) + (temp1 << 8) + temp2);
    }

    /**
     * 把一个4位的数组转化位整形,通常为指令用
     * @param value
     * @return
     */
    public static int fourBytesToInteger(byte[] value) {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        int temp3 = value[3] & 0xFF;
        return ((temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
    }

    /**
     * 截取byte数组
     * @param src
     * @param offset
     * @param length
     * @return
     */
    public static byte[] subByteArr(byte[] src, int offset, int length) {
        byte[] dest = new byte[length];
        System.arraycopy(src, offset, dest, 0, length);
        return dest;
    }

    /**
     * 把一个整形该为1位的byte数组
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static byte[] integerTo1Bytes(int value) {
        byte[] result = new byte[1];
        result[0] = (byte)(value & 0xFF);
        return result;
    }

    /**
     * 把一个整形改为2位的byte数组
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static byte[] integerTo2Bytes(int value) {
        byte[] result = new byte[2];
        result[0] = (byte)((value >> 8) & 0xFF);
        result[1] = (byte)(value & 0xFF);
        return result;
    }

    /**
     * 字符串==>BCD字节数组
     *
     * @param str
     * @return BCD字节数组
     */
    public static byte[] string2Bcd(String str) {
        // 奇数,前补零
        if ((str.length() & SysConst.FIGURE_HEX_ONE) == 1)
        {
            str = "0" + str;
        }

        byte[] ret = new byte[str.length() / 2];
        byte[] bs = str.getBytes(CharsetUtil.UTF_8);
        for (int i = 0; i < ret.length; i++)
        {
            byte high = ascII2Bcd(bs[2 * i]);
            byte low = ascII2Bcd(bs[2 * i + 1]);

            ret[i] = (byte)((high << 4) | low);
        }
        return ret;
    }

    private static byte ascII2Bcd(byte asc) {
        if ((asc >= SysConst.CHAR_ZERO) && (asc <= SysConst.CHAR_NINE)){
            return (byte)(asc - '0');
        }
        else if ((asc >= SysConst.LETTER_UPPERCASE_A) && (asc <= SysConst.LETTER_UPPERCASE_F)){
            return (byte)(asc - 'A' + 10);
        }
        else if ((asc >= SysConst.LETTER_LOWERCASE_A) && (asc <= SysConst.LETTER_LOWERCASE_F)){
            return (byte)(asc - 'a' + 10);
        }
        else{
            return (byte)(asc - 48);
        }
    }

    /**
     * 合并byte数组
     * @param bt1
     * @param bt2
     * @return
     */
    public static byte[] bytesMerge(byte[] bt1, byte[] bt2){
        byte[] bt3 = new byte[bt1.length+bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }


    /**
     * To byte array byte [ ].
     *
     * @param hexString the hex string
     * @return the byte [ ]
     */
    public static byte[] hexToByteArray(String hexString) {
        if (StringUtils.isEmpty(hexString)){
            return null;
        }
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index  > hexString.length() - 1){
                return byteArray;
            }
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }


    /**
     * byte[] to Hex string.
     *
     * @param byteArray the byte array
     * @return the string
     */

    public static String bytesToHexString(byte[] byteArray) {
        final StringBuilder hexString = new StringBuilder();
        if (byteArray == null || byteArray.length <= 0){
            return null;
        }
        for (int i = 0; i < byteArray.length; i++) {
            int v = byteArray[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                hexString.append(0);
            }
            hexString.append(hv);
        }
        return hexString.toString().toLowerCase();
    }

    /**
     * 字节转十六进制字符串
     * @author liuwei
     * @return String
     * @exception
     * @date 2018/12/12 15:41
     */
    public static String byteToHex(byte b) {
        String hex = Integer.toHexString(b & SysConst.FIGURE_HEX_FF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }

    /**
     * 十六进制转二进制
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/24 15:38
     */
    public static String hexStrToBinary16(String hexstr){
        String binAddr = Integer.toBinaryString(Integer.parseInt(hexstr, 16));
        String str = StringUtils.leftPad(binAddr, 16, '0');
        return str;
    }
}
